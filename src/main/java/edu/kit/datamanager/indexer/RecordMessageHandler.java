/*
 * Copyright 2018 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.indexer;

import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.messaging.client.handler.IMessageHandler;
//import edu.kit.datamanager.messaging.client.handler.IMessageHandler.RESULT;
import edu.kit.datamanager.messaging.client.util.MessageHandlerUtils;
import edu.kit.datamanager.indexer.consumer.IConsumerEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Pfeil
 */
@Component
public class RecordMessageHandler implements IMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RecordMessageHandler.class);

    @Autowired
    private IConsumerEngine consumer;

    RecordMessageHandler() {
        System.out.println("CONSTRUCT RECORDMESSAGEHANDLER");
    }

    @Override
    public RESULT handle(BasicMessage message) {
        LOG.debug("Successfully received message {}.", message);
        System.out.println("Successfully received message");
        // guards which decide to reject early
        if (message.getEntityName() != "pidrecord") {
            return RESULT.REJECTED;
        }
        if (!MessageHandlerUtils.isAddressed(this.getHandlerIdentifier(), message)) {
            return RESULT.REJECTED;
        }
        LOG.debug("This message is for me!");

        // 1. process using gemma-plugin
        // 2. hand over data to elasticsearch (a consumer impl)
        return RESULT.SUCCEEDED;
    }

    @Override
    public boolean configure() {
        // no configuration necessary
        return true;
    }
}