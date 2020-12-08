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
package edu.kit.datamanager.indexer.messagehandler;

import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.messaging.client.handler.IMessageHandler;
import edu.kit.datamanager.messaging.client.util.MessageHandlerUtils;
import edu.kit.datamanager.indexer.consumer.IConsumerEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Handlebars;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.exception.IndexerException;
import edu.kit.datamanager.indexer.service.impl.MappingService;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Pfeil
 */
@Component
public class MetastoreMessageHandler implements IMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MetastoreMessageHandler.class);

    private Handlebars hb;

    @Autowired
    private ApplicationProperties properties;
    
    @Autowired
    private MappingService mappingService;

    @Autowired
    private IConsumerEngine consumer;

    MetastoreMessageHandler(ApplicationProperties configuration, MappingService mappingService) {
         properties = configuration;
         this.mappingService = mappingService;
    }

    @Override
    public RESULT handle(BasicMessage message) {
          LOG.debug("Successfully received message with routing key {}.", message.getRoutingKey());
          
          String resourceUrlAsString = message.getMetadata().get("resolvingUrl");
          String mappingId = message.getMetadata().get("schemaId");
          if ((resourceUrlAsString == null) || (mappingId == null)) {
            LOG.debug("Reject message: Missing properties!");
            return RESULT.REJECTED;
          }
          if (!MessageHandlerUtils.isAddressed(this.getHandlerIdentifier(), message)) {
            LOG.debug("Reject message: Not addressed correctly!");
            return RESULT.REJECTED;
          }
        try {
          LOG.debug("This message is for me: {}", message);

          URI resourceUri = new URI(resourceUrlAsString);
          
            List<Path> pathWithAllMappings = mappingService.executeMapping(resourceUri, mappingId);
            if (pathWithAllMappings.isEmpty()) {
              return RESULT.FAILED;
            }
          
        } catch (URISyntaxException ex) {
          String errorMessage = String.format("Error downloading content from '%s': %s", resourceUrlAsString, ex.getMessage());
            LOG.error(errorMessage, ex);
            return RESULT.FAILED;
        } catch (IndexerException iex) {
          String errorMessage = String.format("Error while mapping content from '%s': %s", resourceUrlAsString, iex.getMessage());
            LOG.error(errorMessage, iex);
            return RESULT.FAILED;
        }
           return RESULT.SUCCEEDED;
   }

    @Override
    public boolean configure() {
        boolean everythingWorks = true;
        return everythingWorks;
    }
}