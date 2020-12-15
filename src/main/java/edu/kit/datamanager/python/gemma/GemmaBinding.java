/*
* Copyright 2019 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.python.gemma;

import edu.kit.datamanager.clients.SimpleServiceClient;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class GemmaBinding {

  private final static Logger LOGGER = LoggerFactory.getLogger(GemmaBinding.class);

  @Autowired
  ApplicationProperties gemmaConfiguration;

//  String identifier;
//
//  GemmaBinding(String identifier) {
//      this.identifier = identifier;
//  }

  /**
   * Downloads the file behind the given URI and returns its content as a string.
   * 
   * @param resourceURL the given URI
   * @return the content of the file (the body of the response) as a string. null if a problem occurred.
   */
  public Optional<String> downloadResource(URI resourceURL) {
    String content = null;
    try {
    content = SimpleServiceClient
    .create(resourceURL.toString())
    .accept(MediaType.TEXT_PLAIN)
    .getResource(String.class);
    } catch (Throwable tw) {
      LOGGER.error("Error reading URI '" + resourceURL.toString() + "'", tw);
    }
    return Optional.ofNullable(content);
  }

  /**
   * Stores the given content in a file with the given name within the systems temporary directory.
   * @param content the given content
   * @param filename the given filename
   * @return the absolute path to the stored file on success.
   */
  public Optional<Path> storeAsTempFile(String content, String filename) {
    if (content == null || filename == null) {
      LOGGER.error("Did not receive any resource in the response body. Unable to continue.");
      return Optional.empty();
    }

    File directory = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
    File target = new File(directory, filename);
    Path target_path = Paths.get(target.getAbsolutePath());
    try {
      LOGGER.trace("Writing data resource to temporary file {}.", target_path);
      FileOutputStream out = new FileOutputStream(target);
      out.write(content.getBytes());
      out.close();
    } catch (Exception e) {
      LOGGER.error("Failed to write data resource to temporary file.", e);
    }
    return Optional.ofNullable(target_path);
  }

  public Path mapSingleFile(Path filepath, Path schema) {
    // TODO
    return Paths.get("tmp");
  }

    /**
   * Apply a mapping to a metadata file and upload the result to the repository.
   * The appropriate mapping is obtained using the provided contentType, the
   * mapped content is accessible via contentUri, which is expected to be a
   * local file. Finally, the upload destination is determined by entityId and
   * filename.
   *
   * @param contentUri The location of the content.
   * @param contentType The contentType of the content accessible at contentUri.
   * @param entityId The resource identifier the content is related to.
   * @param filename The content filename, which can be different from the local
   * filename in contentUri.
   *
   * @return The final result, which can be returned as final handler result.
   */
  private boolean applyAndUploadMapping(URI contentUri, String contentType, String entityId, String filename) {
//    LOGGER.trace("Calling processAndUploadMapping({}, {}, {}).", contentType, contentUri, entityId);
//    Path mappingFile = getMappingFile(contentType);
//    LOGGER.trace("Obtained mapping file {}. Obtaining output filename.", mappingFile);
//    Path contentPath = Paths.get(contentUri);
//    if(filename.contains(".")){
//      LOGGER.trace("Replacing file extension of filename {}.", filename);
//      filename = filename.substring(0, filename.lastIndexOf(".")) + ".elastic.json";
//    } else{
//      LOGGER.trace("Appending file extension to filename {}.", filename);
//      filename += ".elastic.json";
//    }
//
//    LOGGER.trace("Obtained output filename '{}'. Creating Python mapping process.", filename);
//    ByteArrayOutputStream bout = new ByteArrayOutputStream();
//    int returnCode = PythonUtils.run(gemmaConfiguration.getPythonLocation(), gemmaConfiguration.getGemmaLocation(), bout, bout, mappingFile.toAbsolutePath().toString(), contentPath.toAbsolutePath().toString(), System.getProperty("java.io.tmpdir") + "/" + filename);
//    LOGGER.trace(bout.toString());
//    LOGGER.trace("Python mapping process returned with status {}. Uploading content to repository.", returnCode);
//
//    Path localFile = Paths.get(System.getProperty("java.io.tmpdir"), filename);

 
    return false;
  }

  /**
   * Check if there is a mapping registered for the provided contentType.
   *
   * @param contentType The content type to check.
   *
   * @return TRUE if a mapping exists, FALSE otherwise.
   */
  private boolean hasMapping(String contentType){
    return true;
  }

  /**
   * Upload the mapped content to the repository. The mapped JSON file will be
   * placed at {resourceId}/data/generated/{filename} and will point to
   * localFileUri.
   *
   * @param resourceId The resource id the uploaded file is associated with.
   * @param filename The filename of the target file.
   * @param localFileUri The location of the file on the local file system.
   *
   * @return TRUE if the upload succeeded, FALSE otherwise.
   *
   * @throw IOException If the preparation of the upload failed.
   */
//  private boolean uploadContent(String resourceId, String filename, URI localFileUri) throws IOException{
//    LOGGER.trace("Performing uploadContent({}, {}, {}, {}).", resourceId, filename, localFileUri);
//    ContentInformation info = new ContentInformation();
//    LOGGER.trace("Setting uploader to handler identifier {}.", this.identifier);
//    info.setUploader(this.identifier);
//
//    HttpStatus status = SimpleRepositoryClient.create(gemmaConfiguration.getRepositoryBaseUrl()).uploadData(resourceId, filename, new File(localFileUri), info, true);
//    return HttpStatus.CREATED.equals(status);
//  }
}
