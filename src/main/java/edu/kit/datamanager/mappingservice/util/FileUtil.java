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
package edu.kit.datamanager.mappingservice.util;

import edu.kit.datamanager.clients.SimpleServiceClient;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class FileUtil {

    /**
     * Default value for suffix of temporary files.
     */
    public static final String DEFAULT_SUFFIX = ".tmp";
    /**
     * Default value for prefix of temporary files.
     */
    public static final String DEFAULT_PREFIX = "MappingUtil_";
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final int MAX_LENGTH_OF_HEADER = 100;

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
        try {
            if ((pathToFile != null) && (pathToFile.toFile().exists())) {
                String contentOfFile = FileUtils.readFileToString(pathToFile.toFile(), StandardCharsets.UTF_8);
                String newExtension = guessFileExtension(contentOfFile.getBytes());
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
        prefix = ((prefix == null) || (prefix.trim().isEmpty())) ? DEFAULT_PREFIX : prefix;
        suffix = ((suffix == null) || (suffix.trim().isEmpty())) ? DEFAULT_SUFFIX : suffix;
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

    private static String guessFileExtension(byte[] schema) {
        // Cut schema to a maximum of MAX_LENGTH_OF_HEADER characters.
        int length = schema.length > MAX_LENGTH_OF_HEADER ? MAX_LENGTH_OF_HEADER : schema.length;
        String schemaAsString = new String(schema, 0, length);
        LOGGER.trace("Guess type for '{}'", schemaAsString);

        Matcher m = JSON_FIRST_BYTE.matcher(schemaAsString);
        if (m.matches()) {
            return ".json";
        } else {
            m = XML_FIRST_BYTE.matcher(schemaAsString);
            if (m.matches()) {
                return ".xml";
            }
        }
        return null;
    }

    public static void cloneGitRepository(String repositoryUrl, String targetDirectory, String branch) {
        LOGGER.info("Cloning branch '{}' of repository '{}' to '{}'", branch, repositoryUrl, targetDirectory);
        File target = new File(targetDirectory);
        target.mkdirs();
        try {
            Git.cloneRepository().setURI(repositoryUrl).setBranch(branch).setDirectory(target).call();
        } catch (GitAPIException ex) {
            throw new MappingException("Error cloning git repository '" + repositoryUrl + "' to '" + targetDirectory + "'!", ex);
        }
    }

//    public static void downloadFile(String url, Path target) throws IOException {
//        try (InputStream in = new URL(url).openStream()) {
//            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
//        }
//    }
//
//    public static void unzip(Path zipFilePath, String destDir) {
//        File dir = new File(destDir);
//        // create output directory if it doesn't exist
//        if(!dir.exists()) dir.mkdirs();
//        FileInputStream fis;
//        //buffer for read and write data to file
//        byte[] buffer = new byte[1024];
//        try {
//            fis = new FileInputStream(zipFilePath.toString());
//            ZipInputStream zis = new ZipInputStream(fis);
//            ZipEntry ze = zis.getNextEntry();
//            while(ze != null){
//                String fileName = ze.getName();
//                File newFile = new File(destDir + File.separator + fileName);
//                System.out.println("Unzipping to "+newFile.getAbsolutePath());
//                //create directories for sub directories in zip
//                newFile.mkdirs();
//                FileOutputStream fos = new FileOutputStream(newFile);
//                int len;
//                while ((len = zis.read(buffer)) > 0) {
//                    fos.write(buffer, 0, len);
//                }
//                fos.close();
//                //close this ZipEntry
//                zis.closeEntry();
//                ze = zis.getNextEntry();
//            }
//            //close last ZipEntry
//            zis.closeEntry();
//            zis.close();
//            fis.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
