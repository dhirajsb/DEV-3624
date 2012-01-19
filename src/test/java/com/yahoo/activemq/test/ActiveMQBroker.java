package com.yahoo.activemq.test; /**
 * Created by IntelliJ IDEA.
 * User: praveenr
 * Date: 8/27/11
 * Time: 2:28 AM
 * To change this template use File | Settings | File Templates.
 */


import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.configuration.XMLConfiguration;

import java.net.URI;

@Deprecated
public class ActiveMQBroker{
    private BrokerService broker=null;
    String activeMQBase = null;
        String log4jProperties = null;
        String brokerConfigURI = null;
    XMLConfiguration config = null;
    public ActiveMQBroker(XMLConfiguration cfg)throws Exception
    {

        //if config name as a "." escape it with an extra dot like this ".."
        activeMQBase = cfg.getString("activemq..base");
        log4jProperties = cfg.getString("log4j..configuration");
        brokerConfigURI = cfg.getString("broker..config..uri");


        System.setProperty("activemq.base", activeMQBase);
        System.setProperty("log4j.configuration",log4jProperties);
        System.setProperty("org.apache.activemq.UseDedicatedTaskRunner","false");

        URI brokerUri =new URI(brokerConfigURI);
        broker=BrokerFactory.createBroker(brokerUri);
        broker.start();
        broker.waitUntilStarted();
        System.out.println("broker started"+broker.isStarted()+" with config "+activeMQBase);
        this.config = cfg;
    }
    public void stopBroker() throws Exception
    {
       System.out.println(activeMQBase + " stopping broker .....");
       broker.stop();
       broker.waitUntilStopped();
       System.out.println("broker stopped");
    }

}
