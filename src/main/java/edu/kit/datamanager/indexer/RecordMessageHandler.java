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
import edu.kit.datamanager.messaging.client.util.MessageHandlerUtils;
import edu.kit.datamanager.indexer.consumer.IConsumerEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    @Override
    public RESULT handle(BasicMessage message) {
        LOG.debug("Successfully received message with routing key {}.", message.getRoutingKey());
        // guards which decide to reject early
        // TODO fix message receiving, ideally without casting.
        //if (message.getEntityName() != "pidrecord") {
        //    LOG.debug("Reject message: Entity name was {}", message.getEntityName());
        //    return RESULT.REJECTED;
        //}
        if (!MessageHandlerUtils.isAddressed(this.getHandlerIdentifier(), message)) {
            LOG.debug("Reject message: Not addressed correctly");
            return RESULT.REJECTED;
        }
        LOG.debug("This message is for me!");

        // 1. process using gemma-plugin
        String record_url = message.getMetadata().get("resolvingUrl");
        // resolve, convert using gemma

        // 2. hand over data to elasticsearch (a consumer impl)
        // Note that gemma can do this, maybe just make a java API for gemma that allows this?
        //  -> In this case, consumers may not be necessary anymore.
        return RESULT.SUCCEEDED;
    }

    @Override
    public boolean configure() {
        // no configuration necessary
        return true;
    }
}