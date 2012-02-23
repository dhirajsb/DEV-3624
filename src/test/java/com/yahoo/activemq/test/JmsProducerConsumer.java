package com.yahoo.activemq.test;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import javax.jms.ConnectionFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

     /*   public void startConsumers()    throws Exception
        {

            if (consumerList == null )consumerList = new ArrayList<JmsConsumer>();
            for (int topic = 0; topic < topicList.size(); topic++)
            {
                String key = "topics.topic("+topic+").num-consumers";
                int numConsumersPerTopic = config.getInt(key);

                key = "topics.topic("+topic+").consumer-tx-size";
                int consumerTxSize = config.getInt(key, 0);

                String topicName = (String)topicList.get(topic);
                    // start consumers before the producers
                    for (int consumers=0; consumers < numConsumersPerTopic ; consumers++ )
                    {
                        String type = config.getString("topics.topic(" + topic + ").type");
                        consumerList.add(new JmsConsumer(connectionFactory, topicName, consumers, type, consumerTxSize));
                    }
            }
        }

	*/
        //MY CODE CHANGE
        public void startConsumers()    throws Exception
        {

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
                key = "topics.topic("+topic+").subscriptions.subscription("+subscription+").num-consumers";
                int numConsumersPerTopic = config.getInt(key);
               // key = "topics.topic("+topic+").subscriptions.subscription("+subscription+").[@name]";
                String subscriptionName = (String)subscriptionList.get(subscription);
                
                    // start consumers before the producers
                    for (int consumers=0; consumers < numConsumersPerTopic ; consumers++ )
                    {
                        consumerList.add(new JmsConsumer(connectionFactory, topicName, consumers, type, consumerTxSize,subscriptionName ,subscription));
                    }
                }    
            }
        }
        public void startProducers()    throws Exception
        {
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
                    // start producers at the end
                    for (int producers=0; producers < numProducersPerTopic; producers++)
                    {
                        String type = config.getString("topics.topic(" + topic + ").type");
                        producerList.add(new JmsProducer(connectionFactory, topicName, producers, producerTxSize, type , arraylist ));
                    }
                }


        }

        public List<Map<Long,Integer>> stopProducers()
        {

            if (producerList == null)
            {
                logger.warn("no producers running");
                return null;
            }
            List<Map<Long,Integer>> stats = new ArrayList<Map<Long,Integer>>();

             // start producers at the end
             for (int producers=0; producers < producerList.size(); producers++)
             {
                   JmsProducer prod = producerList.get(producers);
                   prod.stop();
                   stats.add(prod.getStats());
             }
             producerList = null;

            return stats;
        }




        public List<Map<Long,Integer>> stopConsumers()
        {
            if (consumerList == null )
            {
                logger.warn("no consumers running");
                return null;
            }
            List<Map<Long,Integer>> stats = new ArrayList<Map<Long,Integer>>();
            for (int cons=0; cons < consumerList.size(); cons++)
            {
                  JmsConsumer consumer = consumerList.get(cons);
                  consumer.stop();
                  stats.add(consumer.getStats());
            }
            consumerList = null;
            return stats;
        }




    }

