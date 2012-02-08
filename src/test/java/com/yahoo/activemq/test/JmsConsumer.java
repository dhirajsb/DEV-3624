package com.yahoo.activemq.test; /**
 * Created by IntelliJ IDEA.
 * User: praveenr
 * Date: 8/27/11
 * Time: 2:05 AM
 * To change this template use File | Settings | File Templates.
 */


import org.fusesource.stomp.jms.StompJmsDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

class JmsConsumer implements Runnable {
    protected static Logger logger = LoggerFactory.getLogger(JmsConsumer.class.getName());
    private Thread thread = null;

    Integer consumerId = null;
    private final String type;
    private final int transactionSize;
    private final AtomicLong consumerCounter;
    protected ConnectionFactory connectionFactory = null;
    protected String consumerName = null;
    protected Connection connection = null;
    protected Session session = null;
    protected MessageConsumer consumer = null;
    protected Map<Long, Integer> stats = new HashMap<Long, Integer>();
    protected volatile boolean stopped = false;
    protected String topicName;

    public JmsConsumer(ConnectionFactory connectionFactory, String topicName, int consumerid, String type, int transactionSize, AtomicLong consumerCounter) throws Exception {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
        this.consumerId = consumerid;
        this.type = type;
        this.transactionSize = transactionSize;
        this.consumerCounter = consumerCounter;

        consumerName = "CONSUMER-"+consumerid;
        connection = connectionFactory.createConnection();
        String clientId;
        if ("dsub-shared".equals(type)) {
            clientId = "CLIENT-" + topicName + "-shared";
        } else {
            clientId = "CLIENT-" + topicName + "-" + consumerid;
        }
        connection.setClientID(clientId);
        connection.start();
        thread = new Thread(this, "Thread-" + topicName + "-" + consumerName);
        thread.start();
    }

    public void run() {

        int counter = 0;
        try {
            // session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        	session = connection.createSession(transactionSize >0, Session.AUTO_ACKNOWLEDGE);
        	
            if ("virtual".equals(type)) {
                // ActiveMQ only option:
                // Publishers send to one topic and
                // multiple consumers each get a copy of the messages sent (no load balancing)
                String virtualTopicName = "Consumer." + consumerName + ".VirtualTopic.test." + topicName;
                logger.info("Started "+consumerName+" on queue: "+virtualTopicName);
                Queue dest = session.createQueue(virtualTopicName);
                consumer = session.createConsumer(dest);
            } else if ("virtual-shared".equals(type)) {
                // ActiveMQ only option:
                // Publishers send to one topics and
                // multiple consumers load balancing off one queue.
                String virtualTopicName = "Consumer.shared.VirtualTopic.test." + topicName;
                logger.info("Started "+consumerName+" on queue: "+virtualTopicName);
                Queue dest = session.createQueue(virtualTopicName);
                consumer = session.createConsumer(dest);
            } else if ("queue".equals(type)) {
                // Publishers send to one queue and
                // multiple consumers load balancing off the same queue.
                logger.info("Started "+consumerName+" on queue: "+topicName);
                Queue dest = session.createQueue(topicName);
                consumer = session.createConsumer(dest);
            } else if ("dsub".equals(type)) {
                // Publishers send to one topic and
                // multiple consumers each get a copy of the messages sent (no load balancing)
                logger.info("Started durable "+consumerName+" on dest: "+topicName);
                Topic dest = session.createTopic(topicName);
                consumer = session.createDurableSubscriber(dest, consumerName);
            } else if ("dsub-shared".equals(type)) {
                // An Apollo only option:
                // Publishers send to one topics and
                // multiple consumers load balancing off one queue.
                logger.info("Started durable shared "+consumerName+" on topic: "+topicName);
                Topic dest = session.createTopic(topicName);
                consumer = session.createDurableSubscriber(dest, "shared");
            } else if ("queue-browsed".equals(type)) {

                // An Apollo only option:
                // Publishers send to one queue and
                // multiple consumers browse the queue for messages.  If consumer
                // tracks the sequence position then you get get cheap Exactly Once semantics
                logger.info("Started queue browser "+consumerName+" on queue: "+topicName);
                Queue dest = session.createQueue(topicName);

                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("browser", "true");
                headers.put("browser-end", "false");
                // Messages will have a seq property set. Consumer should track this to 
                // get Exactly Once semantics.
                headers.put("include-seq", "seq");
                // Consumer should set this to the next seq id it wants to receive.  Hopefully
                // a real consumer knows the last seq it processed.
                headers.put("from-seq", "0");
                ((StompJmsDestination) dest).setSubscribeHeaders(headers);

                consumer = session.createConsumer(dest);
            } else {
                throw new IllegalArgumentException("Unknown topic type: "+type);
            }

            long currentTimestamp = System.currentTimeMillis() / 1000;
            int countValue = 0;
            while (!stopped) {
                Message message = consumer.receive(1000L);
                if (message != null) {
                	++counter;

                    consumerCounter.incrementAndGet();
                    ++countValue;
                    long timestamp = System.currentTimeMillis() / 1000;
                    if (currentTimestamp != timestamp) {
                        stats.put(currentTimestamp, countValue);
                        currentTimestamp = timestamp;
                        countValue = 0;
                    }

                    if (session.getTransacted() && counter == transactionSize) {
                    	logger.debug("Committing Transaction.");
                    	session.commit();
                    	counter = 0;
                    }
                }
            }

            // ensure the final countValue makes it into the stats
            stats.put(currentTimestamp, countValue);

            if (session.getTransacted()) {
                session.commit();
            }

        } catch (Exception ex) {
            logger.error("Error receiving messages: " + ex.getMessage(), ex);
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                logger.error("Error closing connection: " + e.getMessage(), e);
            }
            synchronized (this) {
                notify();
                stopped = true;
            }
        }
    }

    public synchronized void stop() {
        if (!stopped) {
            stopped = true;
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public Map<Long, Integer> getStats() {

        return stats;
    }

}
