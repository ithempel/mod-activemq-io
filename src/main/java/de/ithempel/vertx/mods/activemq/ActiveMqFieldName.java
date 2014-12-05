/**
 * mod-activemq-io
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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ithempel.vertx.mods.activemq;

/**
 * Enumeration of field names used by the ActiveMQ module to access fields in Json messages.
 *
 * @author https://github.com/ithempel[Sebastian Hempel]
 */
public enum ActiveMqFieldName {

    /**
     * Command to be executed by the ActiveMQ module.
     *
     * For now the following commands are specified:
     *
     * send:: Send a message to the message broker.
     * subscribe:: Subscribe for message from the message broker.
     */
    COMMAND("command"),
    /**
     * Adress of the destination of the message on the broker. This will normaly be the name
     * of the queue to which the message should be send or to that the module should subscribe.
     */
    DESTINATION("destination"),
    /**
     * Address of the subscriber to a message queue. The address will be used to send message
     * to the subscriber over the internal event bus.
     */
    SUBSCRIBER_ADDRESS("subscriber-address"),
    /**
     * The body of the message that should be send to the message broker.
     */
    BODY("body");

    private String fieldName;

    private ActiveMqFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }

}
