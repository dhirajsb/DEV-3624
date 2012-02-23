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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.yahoo.activemq.*;
public class JmsProducer implements Runnable {
    private Thread t = null;

    protected ConnectionFactory connectionFactory = null;
    protected BytesMessage message = null;
    protected String producerName = null;
    protected Connection connection = null;
    protected Session session = null;
    protected MessageProducer producer = null;
    protected volatile boolean stopped = false;
    protected String topicName;
    protected Integer producerId = null;
    protected static Logger logger = LoggerFactory.getLogger(JmsProducer.class.getName());
    protected volatile boolean done;
    protected int transactionSize;
    private final String type;
    public int[] arraylist = null;
    private AtomicInteger produceCount;
    private long runDuration;


    public JmsProducer(ConnectionFactory connectionFactory, String topicName, int producerid, int transactionSize, String type , int[] arraylist, Integer runDuration, AtomicInteger prodCount) throws Exception {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
        this.producerId = producerid;
        this.transactionSize = transactionSize;
        this.type = type;
        this.arraylist = arraylist;
        this.produceCount = prodCount;
        this.runDuration = runDuration*1000L;
        
        

        producerName = "PRODUCER-" + producerid;
        connection = connectionFactory.createConnection();
        connection.start();
        t = new Thread(this);
        t.start();
    }

	public void run() {

		try {

			session = connection.createSession(transactionSize > 0 ? true
					: false, Session.AUTO_ACKNOWLEDGE);

			if ("virtual".equals(type)) {
				// ActiveMQ only option:
				// Publishers send to one topic and
				// multiple consumers each get a copy of the messages sent (no
				// load balancing)
				String virtualTopicName = "VirtualTopic." + topicName;
				logger.info("Started " + producerName + " on topic: "
						+ virtualTopicName);
				Topic dest = session.createTopic(virtualTopicName);
				producer = session.createProducer(dest);
			} else if ("virtual-shared".equals(type)) {
				// ActiveMQ only option:
				// Publishers send to one topics and
				// multiple consumers load balancing off one queue.
				String virtualTopicName = "VirtualTopic.test." + topicName;
				logger.info("Started " + producerName + " on topic: "
						+ virtualTopicName);
				Topic dest = session.createTopic(virtualTopicName);
				producer = session.createProducer(dest);
			} else if ("queue".equals(type)) {
				// Publishers send to one queue and
				// multiple consumers load balancing off the same queue.
				logger.info("Started " + producerName + " on queue: "
						+ topicName);
				Queue dest = session.createQueue(topicName);
				producer = session.createProducer(dest);
			} else if ("dsub".equals(type)) {
				// Publishers send to one topic and
				// multiple consumers each get a copy of the messages sent (no
				// load balancing)
				logger.info("Started " + producerName + " on topic: "
						+ topicName);
				Topic dest = session.createTopic(topicName);
				producer = session.createProducer(dest);
			} else if ("dsub-shared".equals(type)) {
				// An Apollo only option:
				// Publishers send to one topics and
				// multiple consumers load balancing off one queue.
				logger.info("Started " + producerName + " on topic: "
						+ topicName);
				Topic dest = session.createTopic(topicName);
				producer = session.createProducer(dest);
			} else {
				throw new IllegalArgumentException("Unknown topic type: "
						+ type);
			}

			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			message = session.createBytesMessage();

			byte[] bytes = new byte[70 * 1024];

			// don't have to really create random payload
			// new Random(System.currentTimeMillis()).nextBytes(bytes);
			message.writeBytes(bytes);

			ArrayList<ByteBuffer> byteBufferList = new ArrayList<ByteBuffer>();
			for (int ii = 0; ii < arraylist.length; ii++) {
				byteBufferList
						.add(ii, ByteBuffer.wrap(new byte[arraylist[ii]]));
			}
			// logger.info("payload size " + byteBufferList.get(0).array() );
			message.writeBytes(byteBufferList.get(0).array());
			long msgNumber = 0;

			int cur_idx = 0;
			long stopTime = System.currentTimeMillis() + this.runDuration;
			while (stopTime >= System.currentTimeMillis()) {
				// get data of arrayList[curr_idx] size
				// byte[] data = new byte[arraylist[cur_idx]];
				message.writeBytes(byteBufferList.get(cur_idx).array());
				cur_idx++;
				if (cur_idx == arraylist.length)
					cur_idx = 0;

				long time1 = System.nanoTime();

				producer.send(message);
			//	logger.info("message send " +  message);
				produceCount.getAndIncrement();

				long time2 = System.nanoTime();
				// System.out.println("send from producer "+this.producerName);

				// commit every transactionSize messages
				if (transactionSize > 0 && (++msgNumber % transactionSize) == 0) {
					session.commit();
				}

				long time3 = System.nanoTime();
				if (logger.isDebugEnabled()) {
					logger.debug("time to send " + (time2 - time1)
							+ " time to sync " + (time3 - time2));
				}
			}
			logger.info("Closing the "+ producerName + " of " + topicName +" after " + runDuration / 1000  + " seconds" );

		} catch (Exception ex) {
			logger.error("Error sending messages: " + ex.getMessage(), ex);
		} finally {
			try {
				connection.close();
			} catch (JMSException e) {
				logger.error("Error closing connection: " + e.getMessage(), e);
			}
			
		}
	}

    

    
}


