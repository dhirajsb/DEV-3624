package com.yahoo.activemq.test; /**
 * Created by IntelliJ IDEA.
 * User: praveenr
 * Date: 8/27/11
 * Time: 2:12 AM
 * To change this template use File | Settings | File Templates.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class JmsProducer implements Runnable {
    protected static Logger logger = LoggerFactory.getLogger(JmsProducer.class.getName());
    private Thread thread = null;

    protected ConnectionFactory connectionFactory = null;
    protected BytesMessage message = null;
    protected String producerName = null;
    protected Connection connection = null;
    protected Session session = null;
    protected MessageProducer producer = null;
    protected Map<Long, Integer> stats = new HashMap<Long, Integer>();
    protected volatile boolean stopped = false;
    protected String topicName;
    protected Integer producerId = null;
    protected int transactionSize;
    private final String type;
//    private final AtomicLong produceCounter;


    public JmsProducer(ConnectionFactory connectionFactory, String topicName, int producerid, int transactionSize, String type, AtomicLong produceCounter) throws Exception {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
        this.producerId = producerid;
        this.transactionSize = transactionSize;
        this.type = type;
//        this.produceCounter = produceCounter;

        producerName = "PRODUCER-" + producerid;
        connection = connectionFactory.createConnection();
        connection.start();
        thread = new Thread(this, "Thread-" + topicName + "-" + producerName);
        thread.start();
    }

    public void run() {
        try {

            session = connection.createSession(transactionSize > 0 ? true : false, Session.AUTO_ACKNOWLEDGE);

            if ("virtual".equals(type)) {
                // ActiveMQ only option:
                // Publishers send to one topic and
                // multiple consumers each get a copy of the messages sent (no load balancing)
                String virtualTopicName = "VirtualTopic.test." + topicName;
                logger.info("Started "+producerName+" on topic: "+virtualTopicName);
                Topic dest = session.createTopic(virtualTopicName);
                producer = session.createProducer(dest);
            } else if ("virtual-shared".equals(type)) {
                // ActiveMQ only option:
                // Publishers send to one topics and
                // multiple consumers load balancing off one queue.
                String virtualTopicName = "VirtualTopic.test." + topicName;
                logger.info("Started "+producerName+" on topic: "+virtualTopicName);
                Topic dest = session.createTopic(virtualTopicName);
                producer = session.createProducer(dest);
            } else if ("queue".equals(type)) {
                // Publishers send to one queue and
                // multiple consumers load balancing off the same queue.
                logger.info("Started "+producerName+" on queue: "+topicName);
                Queue dest = session.createQueue(topicName);
                producer = session.createProducer(dest);
            } else if ("dsub".equals(type)) {
                // Publishers send to one topic and
                // multiple consumers each get a copy of the messages sent (no load balancing)
                logger.info("Started "+producerName+" on topic: "+topicName);
                Topic dest = session.createTopic(topicName);
                producer = session.createProducer(dest);
            } else if ("dsub-shared".equals(type)) {
                // An Apollo only option:
                // Publishers send to one topics and
                // multiple consumers load balancing off one queue.
                logger.info("Started "+producerName+" on topic: "+topicName);
                Topic dest = session.createTopic(topicName);
                producer = session.createProducer(dest);
            } else if ("queue-browsed".equals(type)) {
                // An Apollo only option:
                // Publishers send to one queue and
                // multiple consumers browse the queue for messages.  If consumer
                // tracks the sequence position then you get get cheap Exactly Once semantics
                logger.info("Started "+producerName+" on queue: "+topicName);
                Queue dest = session.createQueue(topicName);
                producer = session.createProducer(dest);
            } else {
                throw new IllegalArgumentException("Unknown topic type: "+type);
            }

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            message = session.createBytesMessage();
            byte[] bytes = new byte[1024 * 6];
            new Random(System.currentTimeMillis()).nextBytes(bytes);
            message.writeBytes(bytes);

            int msgNumber = 0;
            long currentTimestamp = System.currentTimeMillis() / 1000;
            int countValue = 0;
            while (!stopped) {
                long time1 = 0, time2 = 0;
                if (logger.isDebugEnabled()) {
                    time1 = System.nanoTime();
                    producer.send(message);
                    time2 = System.nanoTime();
                } else {
                    producer.send(message);
                }

//                produceCounter.incrementAndGet();
                ++countValue;
                long timestamp = System.currentTimeMillis() / 1000;
                if (currentTimestamp != timestamp) {
                    stats.put(currentTimestamp, countValue);
                    currentTimestamp = timestamp;
                    countValue = 0;
                }

                // commit every transactionSize messages
                if (transactionSize > 0 && (++msgNumber > transactionSize)) {
                    session.commit();
                    msgNumber = 0;
                }

                if (logger.isDebugEnabled()) {
                    long time3 = System.nanoTime();
                    logger.debug("time to send " + (time2 - time1) + " time to sync " + (time3 - time2));
                }
            }

            // ensure the final countValue makes it into the stats
            stats.put(currentTimestamp, countValue);

            if (transactionSize > 0) {
                session.commit();
            }

        } catch (Exception ex) {
            logger.error("Error sending messages: " + ex.getMessage(), ex);
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
