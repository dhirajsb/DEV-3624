package com.yahoo.activemq.test; /**
 * Created by IntelliJ IDEA.
 * User: praveenr
 * Date: 8/27/11
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class JmsTest implements Runnable {

    private static Logger logger = Logger.getLogger(JmsTest.class.getName());
    private static Integer ProducerRunDuration = 1000*1; //number of seconds to run the tests
    private static Integer consumerRunDuration = 1000*1;
    private static List<Map<Long,Integer>> consumerStats = null;
    private static List<Map<Long,Integer>> producerStats = null;
    private static XMLConfiguration config = null;
    private static String statsDirName = null;
    public static Map<String, AtomicInteger>prodCount = new HashMap<String,AtomicInteger>();
    public static Map<String, AtomicInteger>consumeCount = new HashMap<String, AtomicInteger>();
    public static Map<String, AtomicInteger>totalProdCount = new HashMap<String,AtomicInteger>();
    public static Map<String, AtomicInteger>totalConsumeCount = new HashMap<String, AtomicInteger>();
    public static Integer runDuration = 0;
    Thread t = new Thread(this);
    

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
           
            statsDirName = config.getString("run-stats-directory");


//            broker = new ActiveMQBroker(config);

            ConnectionFactory connectionFactory = createConnectionFactory(config);

            vmProdCons = new JmsProducerConsumer(connectionFactory, config);

           

            runDuration = vmProdCons.startConsumers();
            Integer prodRunDuration = vmProdCons.startProducers();
            if (runDuration < prodRunDuration)
            {
            	runDuration = prodRunDuration; 
            }
            t.start();
            

 
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

    public void run()
 {

		try {
			long stopTime = (System.currentTimeMillis() + runDuration * 1000L);
			File statsDir = new File(statsDirName);
			if (!statsDir.exists())
				statsDir.mkdirs();

			Long time = System.currentTimeMillis() / 1000;
			String profile = config.getString("profile-name");
			String prodPrefix = profile + time + ".producer";
			String consPrefix = profile + time + ".consumer";
			String suffix = ".csv";
			// Get the the profile name and create the right prefix
			File prodStatsFile = new File(statsDir, prodPrefix + suffix);

			File consStatsFile = new File(statsDir, consPrefix + suffix);

			FileWriter prodfw = new FileWriter(prodStatsFile);
			// write header line first
			// String hdrLine = "producer,time,msgCount\n";
			String hdrLine = "tpoic , timestamp  , msgCount\n";
			prodfw.write(hdrLine);
			FileWriter consfw;
			consfw = new FileWriter(consStatsFile);
			// hdrLine = "consumer,time,msgCount\n";
			hdrLine = "topic , time , msgCount\n";
			consfw.write(hdrLine);
			Map<String, AtomicInteger>tempprodCount = new HashMap<String,AtomicInteger>();
		    Map<String, AtomicInteger>tempconsumeCount = new HashMap<String, AtomicInteger>();

			while (stopTime >= System.currentTimeMillis()) {
				for ( Map.Entry<String, AtomicInteger> e : prodCount.entrySet())
				{	
					AtomicInteger ai = new AtomicInteger(e.getValue().get());
					tempprodCount.put(e.getKey(), ai);
				}
				for ( Map.Entry<String, AtomicInteger> e : consumeCount.entrySet())
				{	
					AtomicInteger ai = new AtomicInteger(e.getValue().get());
					tempconsumeCount.put(e.getKey(), ai);
				}
				Integer oldcount;
				Thread.sleep(1000);

				for (String key : prodCount.keySet()) {

					AtomicInteger ai = prodCount.get(key);
					AtomicInteger total = totalProdCount.get(key);
					Integer currProdCount = ai.getAndSet(0);
					AtomicInteger xi = tempprodCount.get(key);
					oldcount = xi.intValue();
				//	ai.set(0);
					if (currProdCount == 0)
						continue;
					total.addAndGet(currProdCount);
					String str = key + " , " + (System.currentTimeMillis() / 1000 ) + "," + (currProdCount-oldcount) + "\n";
					prodfw.write(str);
					prodfw.flush();

				}
				for (String key : consumeCount.keySet()) {
					AtomicInteger ai = consumeCount.get(key);
					Integer currConsumeCount = ai.getAndSet(0);
					AtomicInteger xi = tempconsumeCount.get(key);
					AtomicInteger total = totalConsumeCount.get(key);
					oldcount = xi.intValue();
					if ((currConsumeCount-oldcount) == 0)
						continue;
					
					total.addAndGet(currConsumeCount);

					String str = key + " , " + (System.currentTimeMillis() / 1000 ) + "," + (currConsumeCount-oldcount) + "\n";
					
					consfw.write(str);
					consfw.flush();
				}
				//prodfw.flush();
				//consfw.flush();
				
			}
			for (String key : totalProdCount.keySet()) {
				System.out.println("Total messages produced for topic " + key
						+ " = " + totalProdCount.get(key));
			}
			for (String key : totalConsumeCount.keySet()) {
				System.out
						.println("Total messages consumed for topic/subscription "
								+ key + " = " + totalConsumeCount.get(key));
			}

			consfw.close();
			prodfw.close();

		} catch (Exception e) {
			logger.info("Error occured " + e.getMessage());
		}

	}
    
    
    protected  static ConnectionFactory createConnectionFactory(XMLConfiguration config) {
        String brokerUrl=config.getString("broker-url");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setUseAsyncSend(true);
        connectionFactory.setCopyMessageOnSend(true);
        return connectionFactory;
    }

}

