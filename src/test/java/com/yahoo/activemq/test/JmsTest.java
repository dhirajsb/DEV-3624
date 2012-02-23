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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class JmsTest {

    private static Logger logger = Logger.getLogger(JmsTest.class.getName());
    private static Integer ProducerRunDuration = 1000*1; //number of seconds to run the tests
    private static Integer consumerRunDuration = 1000*1;
    private static List<Map<Long,Integer>> consumerStats = null;
    private static List<Map<Long,Integer>> producerStats = null;
    private static XMLConfiguration config = null;
    private static String statsDirName = null;

    public void exec(String[] argv)
    {
        if (argv == null || argv.length == 0 )
        {
            logger.error("please pass topic configuration file");
            System.out.println("Please pass topic configuration file in the command line");
            System.exit(-1);
        }

        JmsProducerConsumer vmProdCons = null;
//        ActiveMQBroker broker = null;
        config = null;
        try {
            config = new XMLConfiguration(argv[0]);

            // value mentioned in config is in seconds, convert that into milli-seconds
            ProducerRunDuration = config.getInt("Producer-run-duration")*1000;
            consumerRunDuration = config.getInt("consumer-run-duration")*1000;
            statsDirName = config.getString("run-stats-directory");


//            broker = new ActiveMQBroker(config);

            ConnectionFactory connectionFactory = createConnectionFactory(config);

            vmProdCons = new JmsProducerConsumer(connectionFactory, config);

            System.out.println("Running producers for "+ ProducerRunDuration/1000 +" seconds");
            System.out.println("Running consumers for "+ consumerRunDuration/1000 +" seconds");

            vmProdCons.startConsumers();
            vmProdCons.startProducers();
            if(ProducerRunDuration < consumerRunDuration)
            {
            	Thread.sleep(ProducerRunDuration);
            	System.out.println("stopping all producers after "+ProducerRunDuration/1000 +" seconds");
                producerStats = vmProdCons.stopProducers();
                System.out.println("stopped all producers");
                Thread.sleep(consumerRunDuration-ProducerRunDuration);
                System.out.println("stopping all consumers after "+consumerRunDuration/1000 +" seconds");
                consumerStats =  vmProdCons.stopConsumers();
                System.out.println("stopped all consumers");
           }
            else if (ProducerRunDuration.equals(consumerRunDuration))
            {
              Thread.sleep(ProducerRunDuration);
              System.out.println("stopping producer and consumer after " + ProducerRunDuration/1000 + " seconds");
              System.out.println("stopping producers...");
              producerStats = vmProdCons.stopProducers();
              System.out.println("stopped all producers");
              System.out.println("stopping all consumers after "+consumerRunDuration/1000 +" seconds");
              consumerStats =  vmProdCons.stopConsumers();
              System.out.println("stopped all consumers");
              
            }
            else
            {
            	Thread.sleep(consumerRunDuration);
                System.out.println("stopping all consumers after "+consumerRunDuration/1000 +" seconds");
                consumerStats =  vmProdCons.stopConsumers();
                System.out.println("stopped all consumers");
                Thread.sleep(ProducerRunDuration-consumerRunDuration);
                System.out.println("stopping producer after " + ProducerRunDuration/1000 + " seconds");
                System.out.println("stopping producers...");
                producerStats = vmProdCons.stopProducers();
                System.out.println("stopped all producers");
            }
            
/*
            System.out.println("stopping consumers...");
            consumerStats =  vmProdCons.stopConsumers();
            System.out.println("stopped all consumers");
            if (ProducerRunDuration > consumerRunDuration)
            {

                // run the the test for specific duration
                Thread.sleep(ProducerRunDuration - consumerRunDuration);
            }

            if (producerStats == null)
            {
                System.out.println("stopping all producers after "+ProducerRunDuration/1000 +" seconds");
                producerStats = vmProdCons.stopProducers();
                System.out.println("stopped all producers");
            }
*/
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
        System.out.println("produced " + prodMsgCount + " messages in " + ProducerRunDuration/1000 + " seconds "+(prodMsgCount*1000.0/ProducerRunDuration)+" msg/sec");


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
        System.out.println("consumed " + consMsgCount + " messages in " + consumerRunDuration/1000 + " seconds "+(consMsgCount*1000.0/consumerRunDuration)+" msg/sec");



    }

}

