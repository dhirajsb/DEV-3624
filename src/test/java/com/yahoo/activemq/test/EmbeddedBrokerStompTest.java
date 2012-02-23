package com.yahoo.activemq.test; /**
 * Created by IntelliJ IDEA.
 * User: praveenr
 * Date: 8/27/11
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
public class EmbeddedBrokerStompTest {

    private static Logger logger = Logger.getLogger(EmbeddedBrokerStompTest.class.getName());
    private static Integer totalRunDuration = 1000*1; //number of seconds to run the tests
    private static Integer consumerRunDuration = 1000*1;
    private static List<Map<Long,Integer>> consumerStats = null;
    private static List<Map<Long,Integer>> producerStats = null;
    private static XMLConfiguration config = null;
    private static String statsDirName = null;
    public static void main(String[] argv)
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
            totalRunDuration = config.getInt("total-run-duration")*1000;
            consumerRunDuration = config.getInt("consumer-run-duration")*1000;
            statsDirName = config.getString("run-stats-directory");





//            broker = new ActiveMQBroker(config);

            System.out.println("Running broker for "+ totalRunDuration/1000 +" seconds");
            Thread.sleep(totalRunDuration);

            /**
            vmProdCons = new com.yahoo.activemq.test.JmsProducerConsumer(config);

            System.out.println("Running producers for "+ totalRunDuration/1000 +" seconds");
            System.out.println("Running consumers for "+ consumerRunDuration/1000 +" seconds");

            vmProdCons.startConsumers();
            vmProdCons.startProducers();

            Thread.sleep(consumerRunDuration);
            if (totalRunDuration.equals(consumerRunDuration))
            {
              System.out.println("stopping producer and consumer after " + totalRunDuration/1000 + " seconds");
              System.out.println("stopping producers");
              producerStats = vmProdCons.stopProducers();
              System.out.println("stopped all producers");
              Thread.sleep(500);
            }
            else
            {
                System.out.println("stopping all consumers after "+consumerRunDuration/1000 +" seconds");
            }


            consumerStats =  vmProdCons.stopConsumers();
            System.out.println("stopped all consumers");
            if (totalRunDuration > consumerRunDuration)
            {

                // run the the test for specific duration
                Thread.sleep(totalRunDuration - consumerRunDuration);
            }

            if (producerStats == null)
            {
                System.out.println("stopping all producers after "+totalRunDuration/1000 +" seconds");
                producerStats = vmProdCons.stopProducers();
                System.out.println("stopped all producers");
            }

            //first record stats and then stop the broker
            recordStats(producerStats,consumerStats);
            */
//            broker.stopBroker();

        }
        catch (Exception e)
        {
            System.out.print("Caught exception" + e + "\nstacktrace\n" + e.getStackTrace());
            e.printStackTrace();
/*
        } finally
        {
            try {
                if (broker != null ) broker.stopBroker();
            }
            catch (Exception e)
             {
                System.out.print("Caught exception" + e + "\nstacktrace\n" + e.getStackTrace());
                e.printStackTrace();
            }
*/
        }
        return;
    }

    private static void recordStats(List<Map<Long,Integer>> pStats, List<Map<Long,Integer>> cStats)  throws Exception
    {
        File statsDir = new File(statsDirName);
        Long time =  System.currentTimeMillis()/1000;
        String prodPrefix = time+".producer";
        String consPrefix = time+".consumer";
        String suffix = ".csv";
        File prodStatsFile = new File(statsDir,prodPrefix+suffix);

        File consStatsFile = new File (statsDir,consPrefix+suffix);

        FileWriter fw = new FileWriter(prodStatsFile);
        //write header line first
        String hdrLine = "producer,time,msgCount\n";
        fw.write(hdrLine);
        Long prodMsgCount = new Long(0);
        for (Map<Long,Integer> stat : pStats)
        {
             Set<Long> keys = (Set<Long>) stat.keySet();

            for (Long t: keys)
            {
                Integer count = stat.get(t);
                prodMsgCount += count;
                String str = "producer,"+t+","+count+"\n";
                fw.write(str);
            }
        }
        fw.close();
        System.out.println("produced " + prodMsgCount + " messages in " + totalRunDuration/1000 + " seconds");


        Long consMsgCount = new Long(0);
        fw = new FileWriter(consStatsFile);
        hdrLine = "consumer,time,msgCount\n";
        fw.write(hdrLine);
        for (Map<Long,Integer> stat : cStats)
        {
             Set<Long> keys = (Set<Long>) stat.keySet();

            for (Long t: keys)
            {
                Integer count = stat.get(t);
                consMsgCount += count;
                String str = "consumer,"+t+","+count+"\n";
                fw.write(str);
            }
        }
        fw.close();
        System.out.println("consumed " + consMsgCount + " messages in " + consumerRunDuration/1000 + " seconds");



    }

}
