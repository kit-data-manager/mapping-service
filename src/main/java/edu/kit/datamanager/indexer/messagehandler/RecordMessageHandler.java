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

import java.util.Optional;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.service.impl.MappingService;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Pfeil
 */
@Component
public class RecordMessageHandler implements IMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RecordMessageHandler.class);

    private Handlebars hb;

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private IConsumerEngine consumer;

    RecordMessageHandler(ApplicationProperties configuration) {
         properties = configuration;
    }

    @Override
    public RESULT handle(BasicMessage message) {
        LOG.debug("Successfully received message with routing key {}.", message.getRoutingKey());
        
        String resourceUrlAsString = message.getMetadata().get("resolvingUrl");
        String mappingId = message.getMetadata().get("schemaId");
        if ((resourceUrlAsString == null) || (mappingId == null)) {
          return RESULT.REJECTED;
        }
        
         // guards which decide to reject early
        // TODO fix message receiving, ideally without casting.
        // if (message.getEntityName() != "pidrecord") {
        // LOG.debug("Reject message: Entity name was {}", message.getEntityName());
        // return RESULT.REJECTED;
        // }
        if (!MessageHandlerUtils.isAddressed(this.getHandlerIdentifier(), message)) {
            LOG.debug("Reject message: Not addressed correctly");
            return RESULT.REJECTED;
        }
        LOG.debug("This message is for me: {}", message);

//        // 1. Download record
//        Optional<String> record_json = this.downloadResource(message.getMetadata().get("resolvingUrl"));
//        if (!record_json.isPresent()) {
//            LOG.debug("No JSON was received.");
//            return RESULT.FAILED;
//        }
//        LOG.debug("Received JSON record: {}", record_json.get());
//
//        // 2. TODO Verify record (do not trust anyone)
//        // - verify schema
//        // - verify that pid resolves to url that was given?
//        String elastic_string = "";
//        try {
//            Template template = hb.compile("mytemplate");
//            JSONObject jsonData = new JSONObject(record_json.get());
//            Context c = Context
//                .newBuilder(template)
//                .combine(jsonData.toMap())
//                .build();
//            elastic_string = template.apply(c);
//            // TODO maybe check the types in here, afterwards, instead of helper function? Or use and extend gemma?
//        } catch (Exception e) {
//            LOG.debug("Could not build and apply template. {}", e);
//            return RESULT.FAILED;  // TODO when to use FAILED and when REJECTED?
//        }
//        LOG.debug("Result for elasticsearch is: {}", elastic_string);
//        // 4. Store elastic version to disk (why?)
//        String pid = message.getEntityId();
//        Optional<String> filename = this.pidToFilename(pid);
//        if (!filename.isPresent()) {
//            LOG.debug("Could not extract filename to store json. Abort.");
//            return RESULT.FAILED;
//        }
//        LOG.trace("Will store content of PID {} into {}", pid, filename.get());
//        this.storeAsElasticFile(record_json.get(), filename.get());
//
//        // 4. Send to elasticsearch (consumer impl?)
//        Optional<String> response = this.uploadToElastic(elastic_string, this.pidToSimpleString(pid));
//        LOG.debug("Elastic says: {}", response);

        return RESULT.SUCCEEDED;
    }

    @Override
    public boolean configure() {
        boolean everythingWorks = true;
        return everythingWorks;
    }
//
//    /**
//     * Transforms a given PID to a filename where the record with this PID can be
//     * stored.
//     * 
//     * @param pid the given PID.
//     * @return if the PID was not empty or null, it will return a filename. Empty
//     *         otherwise.
//     */
//    private Optional<String> pidToFilename(String pid) {
//        if (pid == null || pid.isEmpty()) {
//            return Optional.empty();
//        }
//        pid = this.pidToSimpleString(pid);
//        String filename = String.format("%s%s.json", "record", pid);
//        return Optional.of(filename);
//    }
//
//    private String pidToSimpleString(String pid) {
//        pid = pid.replace('/', '_');
//        pid = pid.replace('\\', '_');
//        pid = pid.replace('|', '_');
//        pid = pid.replace('.', '_');
//        pid = pid.replace(':', '_');
//        pid = pid.replace(',', '_');
//        pid = pid.replace('%', '_');
//        pid = pid.replace('!', '_');
//        pid = pid.replace('$', '_');
//        return pid;
//    }
//
//    /**
//     * Downloads the file behind the given URI and returns its content as a string.
//     * 
//     * @param resourceURL the given URI
//     * @return the content of the file (the body of the response) as a string. null
//     *         if a problem occurred.
//     */
//    private Optional<String> downloadResource(String resourceURL) {
//        URL url;
//        try {
//            url = new URL(resourceURL);
//        } catch (Exception e) {
//            return Optional.empty();
//        }
//        String theResource = SimpleServiceClient
//            .create(url.toString())
//            .accept(MediaType.APPLICATION_JSON)
//            // TODO I think this function actually might throw an exception. Handle?
//            .getResource(String.class);
//        return Optional.of(theResource);
//    }
//
//    private Optional<String> uploadToElastic(String json, String document_id) {
//        String elasticIndex = properties.getElasticIndex();
//        String typeAndId = String.format("/_doc/%s?pretty", document_id);
//        try {
////            URL elasticURL = new URL(properties.getElasticUrl() + elasticIndex + typeAndId);
////            HttpRequest request = HttpRequest.newBuilder()
////                .uri(elasticURL.toURI())
////                .header("Content-Type", "application/json")
////                .PUT(HttpRequest.BodyPublishers.ofString(json))
////                .build();
////            HttpClient client =  HttpClient();
////            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
//////            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            return Optional.of("todo"); //response.body());
//        } catch (Exception e) {
//            LOG.error("Could not send to url", e);
//            return Optional.empty();
//        }
//    }
//
//    /**
//     * Stores the given content in a file with the given name within the systems
//     * temporary directory.
//     * 
//     * @param content  the given content
//     * @param filename the given filename
//     * @return the absolute path to the stored file on success.
//     */
//    private Optional<Path> storeAsTempFile(String content, String filename) {
//        if (content == null || filename == null) {
//            LOG.error("Did not receive any resource in the response body. Unable to continue.");
//            return Optional.empty();
//        }
//
//        File directory = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
//        File target = new File(directory, filename);
//        
//        return this.storeAsFile(content, target);
//    }
//
//    private Optional<Path> storeAsElasticFile(String content, String filename) {
//        String elastic_dir = System.getProperty("user.dir") + properties.getElasticFilesStorage();
//
//        if (content == null || filename == null) {
//            LOG.error("Did not receive any resource in the response body. Unable to continue.");
//            return Optional.empty();
//        }
//
//        File directory = Paths.get(elastic_dir).toFile();
//        File target = new File(directory, filename);
//        return this.storeAsFile(content, target);
//    }
//    
//    private Optional<Path> storeAsFile(String content, File file) {
//        Path target_path = Paths.get(file.getAbsolutePath());
//        try {
//            LOG.trace("Writing data resource to file {}.", target_path);
//            FileOutputStream out = new FileOutputStream(file);
//            out.write(content.getBytes());
//            out.close();
//        } catch (Exception e) {
//            LOG.error("Failed to write data resource to temporary file.", e);
//        }
//        return Optional.ofNullable(target_path);
//    }
}