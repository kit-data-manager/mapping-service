/*
 * Copyright 2022 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.mappingservice.util;

import edu.kit.datamanager.clients.SimpleServiceClient;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.exception.MappingServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

/**
 * Various utility methods for file handling.
 *
 * @author maximilianiKIT
 * @author volkerhartmann
 * @author jejkal
 */
public class FileUtil {

    /**
     * Default value for suffix of temporary files.
     */
    public static final String DEFAULT_SUFFIX = ".txt";
    /**
     * Default value for prefix of temporary files.
     */
    public static final String DEFAULT_PREFIX = "MappingUtil_";

    /**
     * Default mime type if detection fails.
     */
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /**
     * Default file extension used if no file extension could be determined
     * based on the mime type.
     */
    public static final String DEFAULT_FILE_EXTENSION = "bin";

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    /**
     * Header size used to check for JSON or XML.
     */
    private static final int MAX_LENGTH_OF_HEADER = 100;
    /**
     * Few first kilobytes of file allowing Tika to detect extension. They don't
     * state explicitly how many kilobytes they need, so 8 should be fine.
     */
    private static final int FEW_KILO_BYTES_FOR_TIKA = 8 * 1024;

    private static final Pattern JSON_FIRST_BYTE = Pattern.compile("(\\R\\s)*\\s*\\{\\s*\"(.|\\s)*", Pattern.MULTILINE);//^\\s{\\s*\".*");
    private static final Pattern XML_FIRST_BYTE = Pattern.compile("((.|\\s)*<\\?xml[^<]*)?\\s*<\\s*(\\w+:)?\\w+(.|\\s)*", Pattern.MULTILINE);

    /**
     * Downloads or copy the file behind the given URI and returns its path on
     * local disc. You should delete or move to another location afterwards.
     *
     * @param resourceURL the given URI
     * @return the path to the created file.
     */
    public static Optional<Path> downloadResource(URI resourceURL) {
        String content;
        Path downloadedFile = null;
        try {
            if (resourceURL != null) {
                String suffix = FilenameUtils.getExtension(resourceURL.getPath());
                suffix = suffix.trim().isEmpty() ? DEFAULT_SUFFIX : "." + suffix;
                if (resourceURL.getHost() != null) {
                    content = SimpleServiceClient
                            .create(resourceURL.toString())
                            .accept(MediaType.TEXT_PLAIN)
                            .getResource(String.class);
                    downloadedFile = createTempFile("download", suffix);
                    FileUtils.writeStringToFile(downloadedFile.toFile(), content, StandardCharsets.UTF_8);
                } else {
                    // copy local file to new place.
                    File srcFile = new File(resourceURL.getPath());
                    File destFile = FileUtil.createTempFile("local", suffix).toFile();
                    FileUtils.copyFile(srcFile, destFile);
                    downloadedFile = destFile.toPath();
                }
            }
        } catch (Throwable tw) {
            LOGGER.error("Error reading URI '" + resourceURL + "'", tw);
            throw new MappingException("Error downloading resource from '" + resourceURL + "'!", tw);
        }
        downloadedFile = fixFileExtension(downloadedFile);

        return Optional.ofNullable(downloadedFile);
    }

    /**
     * Fix extension of file if possible.
     *
     * @param pathToFile the given URI
     * @return the path to the (renamed) file.
     */
    public static Path fixFileExtension(Path pathToFile) {
        Path returnFile = pathToFile;
        Path renamedFile = pathToFile;
        LOGGER.trace("fixFileExtension({})", pathToFile);
        FileInputStream fin = null;
        try {
            if ((pathToFile != null) && (pathToFile.toFile().exists())) {
                fin = new FileInputStream(pathToFile.toFile());
                byte[] header = fin.readNBytes(FEW_KILO_BYTES_FOR_TIKA);
                fin.close();
                String newExtension = guessFileExtension(pathToFile.getFileName().toString(), header);
                if (newExtension != null) {
                    if (!pathToFile.toString().endsWith(newExtension)) {
                        renamedFile = Paths.get(pathToFile + newExtension);
                        FileUtils.moveFile(pathToFile.toFile(), renamedFile.toFile());
                        returnFile = renamedFile;
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Error moving file '{}' to '{}'.", pathToFile, renamedFile);
        }
        LOGGER.trace("'{}' -> '{}'", pathToFile, returnFile);
        return returnFile;
    }

    /**
     * Create temporary file. Attention: The file will not be removed
     * automatically.
     *
     * @param prefix prefix of the file
     * @param suffix suffix of the file
     * @return Path to file
     * @throws MappingException if an error occurs
     */
    public static Path createTempFile(String prefix, String suffix) {
        Path tempFile;
        prefix = (prefix == null || prefix.trim().isEmpty()) ? DEFAULT_PREFIX : prefix;
        suffix = (suffix == null || suffix.trim().isEmpty() || suffix.trim().equals(".")) ? DEFAULT_SUFFIX : suffix;
        try {
            tempFile = Files.createTempFile(prefix, suffix);
        } catch (IOException ioe) {
            throw new MappingException("Error creating tmp file!", ioe);
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
            throw new MappingException("Error removing file '" + tempFile + "'!", ioe);
        }
    }

    /**
     * Detect the mime type of the file at the given path. If detection fails,
     * application/octet-stream is returned as default.
     *
     * @param file Path to file
     *
     * @return The mime type of application/octet-stream as fallback.
     */
    public static String getMimeType(Path file) {
        Tika tika = new Tika();
        String mimeType = DEFAULT_MIME_TYPE;
        LOGGER.trace("Performing mime type detection for file {}.", file.toString());
        try {
            mimeType = tika.detect(file);
            LOGGER.trace("Detected mime type {} for file {}.", mimeType, file.toString());
        } catch (IOException e) {
            LOGGER.warn("Failed to detect media type for file " + file.toString() + ". Returning application/octet-stream.", e);
        }
        return mimeType;
    }

    /**
     * Detect the mime type of the file at the given path. If detection fails,
     * application/octet-stream is returned as default.
     *
     * @param mimeType The mime type as string.
     *
     * @return The extension if it could be determined by mime type or 'bin'
     * otherwise.
     */
    public static String getExtensionForMimeType(String mimeType) {
        String ext = DEFAULT_FILE_EXTENSION;
        LOGGER.trace("Obtaining mime type for string {}.", mimeType);
        try {
            MimeType type = MimeTypes.getDefaultMimeTypes().forName(mimeType);
            LOGGER.trace("Obtained mime type {}. Getting default extension.", type);
            ext = type.getExtension();
            if (ext.isEmpty()) {
                LOGGER.trace("Returning default extension {}.", ext);
            }
        } catch (MimeTypeException ex) {
            LOGGER.error("Failed to obtain mime type for string {}.", mimeType);
        }
        return ext;
    }

    /**
     * Guess the extension of the file from the first bytes using Apache Tika
     *
     * @param filename The name of the file to support mime type detection.
     * @param fewKilobytesOfFile First few kilobytes of the file.
     * @return Estimated extension. e.g. '.xml'
     */
    private static String guessFileExtension(String filename, byte[] fewKilobytesOfFile) {
        String returnValue = null;
        String headerAsString = new String(fewKilobytesOfFile, 0, Math.min(fewKilobytesOfFile.length, MAX_LENGTH_OF_HEADER));
        LOGGER.trace("Guess type for '{}'", headerAsString);
        Matcher m = JSON_FIRST_BYTE.matcher(headerAsString);
        if (m.matches()) {
            returnValue = ".json";
        } else {
            m = XML_FIRST_BYTE.matcher(headerAsString);
            if (m.matches()) {
                returnValue = ".xml";
            }
        }

        if (returnValue == null) {
            // Use tika library to estimate extension
            LOGGER.trace("Use tika library to estimate extension.");
            Tika tika = new Tika();
            String mimeType;
            mimeType = tika.detect(fewKilobytesOfFile, filename);
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            MimeType estimatedMimeType;
            try {
                estimatedMimeType = allTypes.forName(mimeType);
                returnValue = estimatedMimeType.getExtension(); // .jpg
                LOGGER.trace("Mimetype: '{}', Extension: '{}'", mimeType, returnValue);
            } catch (MimeTypeException ex) {
                LOGGER.error("Unknown mimetype '{}'", mimeType);
            }
        }
        return returnValue;
    }

    /**
     * This method clones a git repository into the provided target folder. If
     * the folder already exists, a pull is performed, otherwise it is created
     * before. Typically, the target folder should be takes from property
     * 'mapping-service.codeLocation' obtained from ApplicationProperties.
     *
     * @param repositoryUrl the url of the repository to clone
     * @param branch the branch to clone
     * @param targetFolder the target folder
     * @return the path to the cloned repository
     */
    public static Path cloneGitRepository(String repositoryUrl, String branch, String targetFolder) {
        File target = new File(targetFolder);
        if (target.exists()) {
            try {
                try (Git g = Git.open(target)) {
                    LOGGER.trace("Repository already exists at {}. Active branch is: {}", target, g.getRepository().getBranch());
                    g.getRepository().close();
                }
            } catch (IOException e) {
                String message = String.format("Folder '%s' already exists but contains not Git repository.", target);
                LOGGER.error(message, e);
                throw new MappingServiceException("Failed to prepare plugin. Plugin code destination already exists but is empty.");
            }
        } else {
            target.mkdirs();

            LOGGER.info("Cloning branch '{}' of repository '{}' to '{}'", branch, repositoryUrl, target.getPath());
            try {
                try (Git res = Git.cloneRepository().setURI(repositoryUrl).setBranch(branch).setDirectory(target).call()) {
                    res.getRepository().close();
                }
            } catch (JGitInternalException | GitAPIException e) {
                LOGGER.error("Error cloning git repository '" + repositoryUrl + "' to '" + target + "'!", e);
                throw new MappingServiceException("Failed to prepare plugin. Plugin code destination not accessible.");
            }
        }
        return target.toPath();
    }
}
