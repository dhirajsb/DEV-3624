package com.yahoo.activemq.test;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import javax.jms.ConnectionFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


    /**
     * Created by IntelliJ IDEA.
     * User: praveenr
     * Date: 8/27/11
     * Time: 2:00 AM
     * To change this template use File | Settings | File Templates.
     */
    public class JmsProducerConsumer {

        private static Logger logger = Logger.getLogger(JmsProducerConsumer.class.getName());

        private final ConnectionFactory connectionFactory;
        protected XMLConfiguration config;

        protected ArrayList<JmsConsumer> consumerList;
        protected ArrayList<JmsProducer> producerList;
        protected List topicList =  null;
        protected long transactionSize = 0;
        public int[] arraylist = null;


        public JmsProducerConsumer(ConnectionFactory connectionFactory, XMLConfiguration config)  throws Exception
        {
            this.connectionFactory = connectionFactory;
            this.config = config;
            topicList =  config.getList("topics.topic.[@name]");
            return;
        }

 
        //MY CODE CHANGE
        public Integer startConsumers()    throws Exception
        {
        	Integer maxRunDuration = 0;

            if (consumerList == null )consumerList = new ArrayList<JmsConsumer>();
            for (int topic = 0; topic < topicList.size(); topic++)
            {
            	String key = "topics.topic("+topic+").subscriptions.subscription[@name]";
            	
                List subscriptionList = config.getList(key);
                key = "topics.topic("+topic+").consumer-tx-size";
                int consumerTxSize = config.getInt(key, 0);
                String topicName = (String)topicList.get(topic);
                String type = config.getString("topics.topic(" + topic + ").type");
                for ( int subscription =0 ; subscription < subscriptionList.size() ; subscription++)
                {
                	Integer runDuration = config.getInt("topics.topic("+topic+").subscriptions.subscription("+subscription+").run-duration");
                	if (maxRunDuration < runDuration)
                	{
                		maxRunDuration = runDuration;
                	}
                key = "topics.topic("+topic+").subscriptions.subscription("+subscription+").num-consumers";
                int numConsumersPerTopic = config.getInt(key);
               // key = "topics.topic("+topic+").subscriptions.subscription("+subscription+").[@name]";
                String subscriptionName = (String)subscriptionList.get(subscription);
                AtomicInteger consumeCount = new AtomicInteger();
                consumeCount.set(0);
                JmsTest.consumeCount.put(topicName+"-"+subscriptionName,consumeCount);
                JmsTest.totalConsumeCount.put(topicName+"-"+subscriptionName, consumeCount);
                    // start consumers before the producers
                    for (int consumers=0; consumers < numConsumersPerTopic ; consumers++ )
                    {
                        consumerList.add(new JmsConsumer(connectionFactory, topicName, consumers, type, consumerTxSize,subscriptionName ,subscription, runDuration,consumeCount));
                    }
                }    
            }
            return maxRunDuration;
        }
        public Integer startProducers()    throws Exception
        {
        	Integer maxRunDuration = 0;
            if (producerList == null ) producerList = new ArrayList<JmsProducer>();
            for (int topic = 0; topic < topicList.size(); topic++)
                {
                    String key = "topics.topic("+topic+").num-producers";
                    int numProducersPerTopic = config.getInt(key);
                    String topicName = (String)topicList.get(topic);

                    key = "topics.topic("+topic+").producer-tx-size";
                    int producerTxSize = config.getInt(key, 0);
                   //MY CHANGE to use different payload
                    
                    key = "topics.topic("+topic+").message-size" ;
                    List message_size = config.getList(key);
                    arraylist = new int[message_size.size()];
                    for ( int i =0 ; i < message_size.size() ; i++)
                    {
                    	arraylist[i] = Integer.parseInt((String)message_size.get(i));
                    }
                    AtomicInteger produceCount = new AtomicInteger();
                    produceCount.set(0);
                    JmsTest.prodCount.put(topicName,produceCount);
                    JmsTest.totalProdCount.put(topicName, produceCount);
                    // start producers at the end
                    
                    Integer runDuration = config.getInt("topics.topic(" + topic + ").run-duration");
                    if (maxRunDuration < runDuration)
                    {
                    	maxRunDuration = runDuration;
                    }
                    
                    for (int producers=0; producers < numProducersPerTopic; producers++)
                    {
                        String type = config.getString("topics.topic(" + topic + ").type");
                        
                        producerList.add(new JmsProducer(connectionFactory, topicName, producers, producerTxSize, type , arraylist, runDuration, produceCount ));
                    }
                }
            
            return maxRunDuration;


        }

      



    }

