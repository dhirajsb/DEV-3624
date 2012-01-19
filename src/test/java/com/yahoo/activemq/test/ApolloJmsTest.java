/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yahoo.activemq.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.configuration.XMLConfiguration;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;

import javax.jms.ConnectionFactory;

/**
 * <p>
 * </p>
 */
public class ApolloJmsTest extends JmsTest {

    public static void main(String[] args) {
        new ApolloJmsTest().exec(args);
    }

    @Override
    protected ConnectionFactory createConnectionFactory(XMLConfiguration config) {
        String brokerUrl=config.getString("broker-url");
        StompJmsConnectionFactory connectionFactory = new StompJmsConnectionFactory();
        connectionFactory.setBrokerURI(brokerUrl);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("password");
        connectionFactory.setForceAsyncSend(true);
        return connectionFactory;
    }
}
