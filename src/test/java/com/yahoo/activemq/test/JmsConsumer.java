package com.yahoo.activemq.test; /**
 * Created by IntelliJ IDEA.
 * User: praveenr
 * Date: 8/27/11
 * Time: 2:05 AM
 * To change this template use File | Settings | File Templates.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class JmsConsumer implements Runnable {
    private Thread t = null;

    Integer consumerId = null;
    private final String type;
    private final int transactionSize;
    protected ConnectionFactory connectionFactory = null;
    protected String consumerName = null;
    protected Connection connection = null;
    protected Session session = null;
    protected MessageConsumer consumer = null;
    
    protected volatile boolean stopped = false;
    protected String topicName;
    protected static Logger logger = LoggerFactory.getLogger(JmsConsumer.class.getName());
    protected String subscriptionName = null;
    protected int subscriptionId ;
    protected AtomicInteger consumeCount;
    long runDuration ;
    
    public JmsConsumer(ConnectionFactory connectionFactory, String topicName, int consumerid, String type, int transactionSize , String subscriptionName , int subscriptionId, Integer runDuration, AtomicInteger consumeCount) throws Exception {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
        this.consumerId = consumerid;
        this.type = type;
        this.transactionSize = transactionSize;
        this.subscriptionName = subscriptionName;
        this.subscriptionId = subscriptionId;
        this.consumeCount = consumeCount;
        this.runDuration = runDuration*1000L;

        consumerName = "CONSUMER-" + consumerid ;
        connection = connectionFactory.createConnection();
        String clientId = "CLIENT-" + topicName + "-" + subscriptionName + "-" + consumerid + "-" + subscriptionId;
        connection.setClientID(clientId);
        connection.start();
        t = new Thread(this);
        t.start();
    }

	public void run() {

		int counter = 0;
		try {
			// session = connection.createSession(false,
			// Session.DUPS_OK_ACKNOWLEDGE);
			session = connection.createSession(transactionSize > 0,
					Session.AUTO_ACKNOWLEDGE);

			if ("virtual".equals(type)) {
				// ActiveMQ only option:
				// Publishers send to one topic and
				// multiple consumers each get a copy of the messages sent (no
				// load balancing)
				String virtualTopicName = "Consumer." + subscriptionName
						+ ".VirtualTopic." + topicName;
				logger.info("Started " + consumerName + " on queue: "
						+ virtualTopicName);
				Queue dest = session.createQueue(virtualTopicName);
				consumer = session.createConsumer(dest);
			} else if ("virtual-shared".equals(type)) {
				// ActiveMQ only option:
				// Publishers send to one topics and
				// multiple consumers load balancing off one queue.
				String virtualTopicName = "Consumer.shared.VirtualTopic.test."
						+ topicName;
				logger.info("Started " + consumerName + " on queue: "
						+ virtualTopicName);
				Queue dest = session.createQueue(virtualTopicName);
				consumer = session.createConsumer(dest);
			} else if ("queue".equals(type)) {
				// Publishers send to one queue and
				// multiple consumers load balancing off the same queue.
				logger.info("Started " + consumerName + " on queue: "
						+ topicName);
				Queue dest = session.createQueue(topicName);
				consumer = session.createConsumer(dest);
			} else if ("dsub".equals(type)) {
				// Publishers send to one topic and
				// multiple consumers each get a copy of the messages sent (no
				// load balancing)
				logger.info("Started durable " + consumerName + " on dest: "
						+ topicName);
				Topic dest = session.createTopic(topicName);
				consumer = session.createDurableSubscriber(dest, consumerName);
			} else if ("dsub-shared".equals(type)) {
				// An Apollo only option:
				// Publishers send to one topics and
				// multiple consumers load balancing off one queue.
				logger.info("Started durable shared " + consumerName
						+ " on topic: " + topicName);
				Topic dest = session.createTopic(topicName);
				consumer = session.createDurableSubscriber(dest, "shared");
			} else {
				throw new IllegalArgumentException("Unknown topic type: "
						+ type);
			}

			long stopTime = System.currentTimeMillis() + runDuration;
			while (stopTime >= System.currentTimeMillis()) {
				
				Message message = consumer.receive(1000L);
				//logger.info("message recieved " + message);
				consumeCount.getAndIncrement();
				// System.out.println("received message "+this.subscriptionName);
				if (message != null) {
					++counter;

					if (session.getTransacted() && counter == transactionSize) {
						logger.debug("Committing Transaction.");
						session.commit();
						counter = 0;
					}
				}
			}
			logger.info("Closing the "+ consumerName + " of " + topicName + "-"+ subscriptionName +" after " + runDuration / 1000 + " seconds" );
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
			
		}
	}
    

}

