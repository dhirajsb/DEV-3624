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
import com.yahoo.activemq.*;
public class JmsProducer implements Runnable {
    private Thread t = null;

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
    protected static Logger logger = LoggerFactory.getLogger(JmsProducer.class.getName());
    protected volatile boolean done;
    protected int transactionSize;
    private final String type;
    public int[] arraylist = null;


    public JmsProducer(ConnectionFactory connectionFactory, String topicName, int producerid, int transactionSize, String type , int[] arraylist) throws Exception {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
        this.producerId = producerid;
        this.transactionSize = transactionSize;
        this.type = type;
        this.arraylist = arraylist;

        producerName = "PRODUCER-" + producerid;
        connection = connectionFactory.createConnection();
        connection.start();
        t = new Thread(this);
        t.start();
    }

    public void run() {
        try {

            session = connection.createSession(transactionSize > 0 ? true : false, Session.AUTO_ACKNOWLEDGE);

            if ("virtual".equals(type)) {
                // ActiveMQ only option:
                // Publishers send to one topic and
                // multiple consumers each get a copy of the messages sent (no load balancing)
                String virtualTopicName = "VirtualTopic." + topicName;
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
            } else {
                throw new IllegalArgumentException("Unknown topic type: "+type);
            }

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            message = session.createBytesMessage();
            
            byte[] bytes = new byte[70*1024];
            
            // don't have to really create random payload
	    // new Random(System.currentTimeMillis()).nextBytes(bytes);
            message.writeBytes(bytes);

            ArrayList<ByteBuffer> byteBufferList = new ArrayList<ByteBuffer>();
            for(int ii = 0; ii < arraylist.length;ii++)
            {
                    byteBufferList.add(ii, ByteBuffer.wrap(new byte[arraylist[ii]]));
            }
           // logger.info("payload size " + byteBufferList.get(0).array() );
            message.writeBytes(byteBufferList.get(0).array());
            long msgNumber = 0;
            long currentTimestamp = System.currentTimeMillis() / 1000;
            int countValue = 0;
            int cur_idx = 0;
            
            while (!stopped) {
            	// get data of arrayList[curr_idx] size
            	//byte[] data = new byte[arraylist[cur_idx]];
            	message.writeBytes(byteBufferList.get(cur_idx).array());
            	cur_idx++;
            	if (cur_idx == arraylist.length)
            		cur_idx = 0;
            	
                long time1 = System.nanoTime();
                producer.send(message);
                long time2 = System.nanoTime();
//                System.out.println("send from producer "+this.producerName);
                ++countValue;
                long timestamp = System.currentTimeMillis() / 1000;
                if (currentTimestamp != timestamp) {
                    stats.put(currentTimestamp, countValue);
                    currentTimestamp = timestamp;
                    countValue = 0;
                }

                // commit every transactionSize messages
                if (transactionSize > 0 && (++msgNumber % transactionSize) == 0) {
                    session.commit();
                }

                long time3 = System.nanoTime();
                if (logger.isDebugEnabled()) {
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
                done = true;
            }
        }
    }

    public synchronized void stop() {
        stopped = true;
        try {
            wait();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public Map<Long, Integer> getStats() {
        return stats;
    }
}

