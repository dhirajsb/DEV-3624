package com.yahoo.activemq.test; /**
 * Created by IntelliJ IDEA.
 * User: praveenr
 * Date: 8/27/11
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;


public abstract class JmsTest {

    private static Logger logger = Logger.getLogger(JmsTest.class.getName());
    private static long producerRunDuration = 1000*1; //number of seconds to run the tests
    private static long consumerRunDuration = 1000*1;
    private static List<Map<Long,Integer>> consumerStats = null;
    private static List<Map<Long,Integer>> producerStats = null;
    private static XMLConfiguration config = null;
    private static String statsDirName = null;
    private static long pollInterval;

    public void exec(String[] argv)
    {
        if (argv == null || argv.length == 0 )
        {
            logger.error("please pass topic configuration file");
            System.out.println("Please pass topic configuration file in the command line");
            System.exit(-1);
        }

//        ActiveMQBroker broker = null;
        config = null;
        try {
            config = new XMLConfiguration(argv[0]);

            // value mentioned in config is in seconds, convert that into milli-seconds
            final long producerDelayDuration = config.getInt("producer-delay-duration", 0)*1000L;
            final long consumerDelayDuration = config.getInt("consumer-delay-duration", 0)*1000L;
            producerRunDuration = config.getInt("producer-run-duration")*1000L;
            consumerRunDuration = config.getInt("consumer-run-duration")*1000L;
            statsDirName = config.getString("run-stats-directory");
            pollInterval = config.getInt("poll-interval", 0)*1000L;


//            broker = new ActiveMQBroker(config);

            ConnectionFactory connectionFactory = createConnectionFactory(config);

            final JmsProducerConsumer vmProdCons = new JmsProducerConsumer(connectionFactory, config);
            final CountDownLatch latch = new CountDownLatch(2);

            // give consumers a head start
            final AtomicLong consumeStart = new AtomicLong();
            Thread consumerThread=null;
            if( consumerRunDuration > 0 ) {
                consumerThread = new Thread("consumer monitor") {
                    @Override
                    public void run() {
                        try {
                            if(consumerDelayDuration > 0 ) {
                                System.out.println("Delaying consumers for "+ consumerDelayDuration /1000 + " seconds");
                                Thread.sleep(consumerDelayDuration);
                            }
                            System.out.println("Running consumers for "+ consumerRunDuration /1000 + " seconds");
                            consumeStart.set(System.currentTimeMillis());
                            vmProdCons.startConsumers();
                            Thread.sleep(consumerRunDuration);
                            System.out.println("stopping consumers");
                            consumerStats =  vmProdCons.stopConsumers();
                            System.out.println("stopped all consumers");
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }
                    }
                };
                consumerThread.start();
            } else {
                latch.countDown();
            }

            final AtomicLong produceStart = new AtomicLong();
            Thread producerThread=null;
            if( producerRunDuration > 0 ) {
                producerThread = new Thread("producer monitor") {
                    @Override
                    public void run() {
                        try {
                            if(producerDelayDuration > 0 ) {
                                System.out.println("Delaying producers for "+ producerDelayDuration /1000 + " seconds");
                                Thread.sleep(producerDelayDuration);
                            }
                            System.out.println("Running producers for "+ producerRunDuration /1000 + " seconds");
                            produceStart.set(System.currentTimeMillis());
                            vmProdCons.startProducers();
                            Thread.sleep(producerRunDuration);
                            System.out.println("stopping producers");
                            producerStats = vmProdCons.stopProducers();
                            System.out.println("stopped all producers");
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }
                    }
                };
                producerThread.start();
            } else {
                latch.countDown();
            }

            if (pollInterval > 0) {
                Thread.sleep(1000);
                long lastProduceCounter=0;
                long lastConsumeCounter=0;
                long lastPollTime = System.currentTimeMillis();
                while(producerThread!=null || consumerThread!=null) {
                    long now = System.currentTimeMillis();
                    if( producerThread!=null ) {
                        if(!producerThread.isAlive()) {
                            producerThread = null;
                            continue;
                        } else {
                            long start = produceStart.get();
                            if(start != 0) {
                                long count = vmProdCons.getProduceCounter();
                                long duration = now - start;
                                long pollDuration = now-lastPollTime;
                                long pollCount = count - lastProduceCounter;
                                lastProduceCounter = count;
                                System.out.println(String.format("produced %d msg in %.2f seconds (%.2f msg/sec) | poll added %d msg (%.2f msg/sec)", count, duration/1000.0, ((count*1000.0)/duration), pollCount, ((pollCount*1000.0)/pollDuration)));

                            }
                        }
                    }
                    if( consumerThread!=null ) {
                        if(!consumerThread.isAlive()) {
                            consumerThread = null;
                            continue;
                        } else {
                            long start = consumeStart.get();
                            if(start != 0) {
                                long count = vmProdCons.getConsumerCounter();
                                long duration = now - start;
                                long pollDuration = now-lastPollTime;
                                long pollCount = count - lastConsumeCounter;
                                lastConsumeCounter = count;
                                System.out.println(String.format("consumed %d msg in %.2f seconds (%.2f msg/sec) | poll added %d msg (%.2f msg/sec)", count, duration/1000.0, ((count*1000.0)/duration), pollCount, ((pollCount*1000.0)/pollDuration)));
                            }
                        }
                    }
                    Thread.sleep(pollInterval);
                    lastPollTime = now;
                }
            }

            // wait for producers and consumers to finish
            try {
                latch.await();
            } catch (InterruptedException e) {
                // ignore
            }

            //first record stats and then stop the broker
            recordStats(producerStats,consumerStats);

//            broker.stopBroker();

        }
        catch (Exception e)
        {
            System.out.print("Caught exception" + e + "\nstacktrace\n" + e.getStackTrace());
            e.printStackTrace();
        }
/*
        finally
        {
            try {
                if (broker != null ) broker.stopBroker();
            }
            catch (Exception e)
             {
                System.out.print("Caught exception" + e + "\nstacktrace\n" + e.getStackTrace());
                e.printStackTrace();
            }
        }
*/
        return;
    }

    abstract protected ConnectionFactory createConnectionFactory(XMLConfiguration config);

    private static void recordStats(List<Map<Long,Integer>> pStats, List<Map<Long,Integer>> cStats)  throws Exception
    {
        File statsDir = new File(statsDirName);
        if (!statsDir.exists())
        	statsDir.mkdirs();
        
        Long time =  System.currentTimeMillis()/1000;
        String prodPrefix = time+".producer";
        String consPrefix = time+".consumer";
        String suffix = ".csv";
        File prodStatsFile = new File(statsDir,prodPrefix+suffix);

        File consStatsFile = new File (statsDir,consPrefix+suffix);

        FileWriter fw = new FileWriter(prodStatsFile);
        //write header line first
//        String hdrLine = "producer,time,msgCount\n";
        String hdrLine = "time,msgCount\n";
        fw.write(hdrLine);
        Long prodMsgCount = new Long(0);
        Map<Long,Integer> aggrStats = new HashMap<Long, Integer>();
        for (Map<Long,Integer> stat : pStats)
        {
             Set<Map.Entry<Long,Integer>> entries = stat.entrySet();

            for (Map.Entry<Long, Integer> entry: entries)
            {
                Long timestamp = entry.getKey();
                Integer count = entry.getValue();
                prodMsgCount += count;
/*
                String str = "producer,"+entry+","+count+"\n";
                fw.write(str);
*/
                Integer aggrCount = aggrStats.get(timestamp);
                if (aggrCount != null) {
                    aggrStats.put(timestamp, aggrCount + count);
                } else {
                    aggrStats.put(timestamp, count);
                }
            }
        }
        for (Map.Entry<Long, Integer> entry : aggrStats.entrySet()) {
            String str = ""+entry.getKey()+","+entry.getValue()+"\n";
            fw.write(str);
        }
        fw.close();
        System.out.println("produced " + prodMsgCount + " messages in " + producerRunDuration /1000 + " seconds");


        Long consMsgCount = new Long(0);
        fw = new FileWriter(consStatsFile);
//        hdrLine = "consumer,time,msgCount\n";
        hdrLine = "time,msgCount\n";
        fw.write(hdrLine);
        aggrStats.clear();
        for (Map<Long,Integer> stat : cStats)
        {
             Set<Map.Entry<Long,Integer>> entries = stat.entrySet();

            for (Map.Entry<Long, Integer> entry: entries)
            {
                Long timestamp = entry.getKey();
                Integer count = entry.getValue();
                consMsgCount += count;
/*
                String str = "consumer,"+entry+","+count+"\n";
                fw.write(str);
*/
                Integer aggrCount = aggrStats.get(timestamp);
                if (aggrCount != null) {
                    aggrStats.put(timestamp, aggrCount + count);
                } else {
                    aggrStats.put(timestamp, count);
                }
            }
        }
        for (Map.Entry<Long, Integer> entry : aggrStats.entrySet()) {
            String str = ""+entry.getKey()+","+entry.getValue()+"\n";
            fw.write(str);
        }
        fw.close();
        System.out.println("consumed " + consMsgCount + " messages in " + consumerRunDuration/1000 + " seconds");



    }

}
