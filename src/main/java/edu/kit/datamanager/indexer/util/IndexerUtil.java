/*
 * Copyright 2020 KIT
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
package edu.kit.datamanager.indexer.util;

import edu.kit.datamanager.clients.SimpleServiceClient;
import edu.kit.datamanager.indexer.exception.IndexerException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 */
public class IndexerUtil {

  /** Default value for suffix of temporary files. */
  public static final String DEFAULT_SUFFIX = ".tmp";
  /** Default value for prefix of temporary files. */
  public static final String DEFAULT_PREFIX = "IndexerUtil_";
  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(IndexerUtil.class);

  /**
   * Downloads the file behind the given URI and returns its path on local disc.
   * You should delete or move to another location afterwards.
   *
   * @param resourceURL the given URI
   * @return the path to the created file.
   */
  public static Optional<Path> downloadResource(URI resourceURL) {
    String content = null;
    Path downloadedFile = null;
    try {
      content = SimpleServiceClient
              .create(resourceURL.toString())
              .accept(MediaType.TEXT_PLAIN)
              .getResource(String.class);
      downloadedFile = createTempFile("gemma", "txt");
      FileUtils.writeStringToFile(downloadedFile.toFile(), content, StandardCharsets.UTF_8);
    } catch (Throwable tw) {
      LOGGER.error("Error reading URI '" + resourceURL.toString() + "'", tw);
      throw new IndexerException("Error downloading resource from '" + resourceURL.toString() + "'!", tw);
    }
    return Optional.ofNullable(downloadedFile);
  }

  /**
   * Create temporary file. Attention: The file will not be removed
   * automatically.
   *
   * @param prefix prefix of the file
   * @param suffix suffix of the file
   * @return Path to file
   * @exception IndexerException if an error occurs
   */
  public static Path createTempFile(String prefix, String suffix) {
    Path tempFile = null;
    prefix = ((prefix == null)|| (prefix.trim().isEmpty())) ? DEFAULT_PREFIX : prefix;
    suffix = ((suffix == null) || (suffix.trim().isEmpty()))  ? DEFAULT_SUFFIX : suffix;
    try {
      tempFile = Files.createTempFile(prefix, suffix);
    } catch (IOException ioe) {
      throw new IndexerException("Error creating tmp file!", ioe);
    }
    return tempFile;
  }

  /**
   * Remove temporary file. 
   *
   * @param tempFile Path to file
   */
  public static void removeFile(Path tempFile) {
    try {
      Files.deleteIfExists(tempFile);
    } catch (IOException ioe) {
      throw new IndexerException("Error removing file '" + tempFile.toString() + "'!", ioe);
    }
    return;
  }

}
