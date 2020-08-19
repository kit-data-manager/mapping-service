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
import edu.kit.datamanager.messaging.client.handler.IMessageHandler.RESULT;
import edu.kit.datamanager.messaging.client.util.MessageHandlerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Pfeil
 */
public class RecordMessageHandler implements IMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RecordMessageHandler.class);

    @Override
    public RESULT handle(BasicMessage message){
        LOG.debug("Successfully received message {}.", message);
        if(MessageHandlerUtils.isAddressed(getHandlerIdentifier(), message)){
            LOG.debug("This message is for me!"); 
            return RESULT.SUCCEEDED;
        } else{
            LOG.debug("I'll ignore this message.");
            return RESULT.REJECTED;
        }
    }

    @Override
    public boolean configure(){
        //no configuration necessary
        return true;
    }
}