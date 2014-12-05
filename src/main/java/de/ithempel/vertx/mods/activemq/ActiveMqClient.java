/**
 * module-activemq-io
 *
 * Copyright 2014 Sebastian Hempel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,"subscriber-address"
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ithempel.vertx.mods.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

/**
 * Client for the module to communicate with the ActiveMQ broker.
 *
 * The class encapsulates all communication with the ActiveMQ library. Objects of this class connect
 * to or disconnect from the broker. There is a method to send a message to the broker. Another method
 * is used to subcribe to a queue.
 *
 * @author https://github.com/ithempel[Sebastian Hempel]
 */
public class ActiveMqClient {

    private final String host;
    private final int port;
    private Connection connection;
    private Session session;

    private final Logger logger;
    private MessageConverter converter;

    public ActiveMqClient(Logger logger, String host, int port) {
        this.logger = logger;

        this.host = host;
        this.port = port;
    }

    /**
     * Connect to the ActiveMQ message broker.
     *
     * The method uses the host address and the port of the constructor. The Session will be opened
     * in the AUTO_ACKNOWLEDGE mode.
     */
    public boolean connect() {
        boolean success = true;

        String url = String.format("tcp://%s:%d", host, port);
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

        try {
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            converter = new MessageConverter(session, logger);

            String debugMessage = String.format("Successfully connect to ActiveMQ broker on %s", url);
            logger.debug(debugMessage);
        }
        catch (JMSException e) {
            success = false;

            String errorMessage = String.format("Cannot connect to ActiveMQ Broker on %s", url);
            logger.error(errorMessage, e);
        }

        return success ;
    }

    /**
     * Disconnect an opened connection to the broker.
     *
     * If there is no open connection this method will do nothing.
     */
    public void disconnect() {
        try {
            connection.close();
        }
        catch (JMSException e) {
            String errorMessage = String.format("Error closing connection to ActiveMQ Broker");
            logger.error(errorMessage, e);
        }
    }

    /**
     * The JsonObject with the message to send will be send to the queue destination of the
     * message broker. Depending on the format of the message the Json structure will be converted
     * to a corresponding JMS Message type.
     *
     * @param destination name of the destination / queue to send the message to
     * @param message message to send as a Json structure
     */
    public void send(String destination, JsonObject message) {
        Message jmsMessage = converter.convertToJmsMessage(message);

        try {
            Queue queue = session.createQueue(destination);
            MessageProducer producer = session.createProducer(queue);

            producer.send(jmsMessage);

            String debugMessage = String.format("Successfully send message of type %s on queue %s",
                    jmsMessage.getClass().getName(), destination);
            logger.debug(debugMessage);
        } catch (JMSException e) {
            String errorMessage = String.format("Error sending message to ActiveMQ broker on queue %s",
                    destination);
            logger.error(errorMessage, e);
        }
    }

    /**
     * Subscribe a Handler to receive messages for the given destination / queue.
     *
     * The method will subscribe the given handler to receive messages from the given queue.
     *
     * @param destination destination / queue to receive messages from
     * @param subscriberHandler handler to call for the received messages
     */
    public void subscribe(String destination, final Handler<JsonObject> subscriberHandler) {
        try {
            Queue queue = session.createQueue(destination);
            MessageConsumer consumer = session.createConsumer(queue);

            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    JsonObject json = converter.convertToJsonObject(message);
                    String debugMessage = String.format(
                            "Received and converted message of type %s from ActiveMQ broker",
                            message.getClass().getName());
                    logger.debug(debugMessage);

                    subscriberHandler.handle(json);
                }
            });

            String infoMessage = String.format("Successfully subscribed to queue %s", destination);
            logger.info(infoMessage);
        } catch (JMSException e) {
            String errorMessage = String.format("Error subscribing to queue %s", destination);
            logger.error(errorMessage, e);
        }
    }

}
