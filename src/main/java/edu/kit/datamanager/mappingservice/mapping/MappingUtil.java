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
package edu.kit.datamanager.mappingservice.mapping;

import edu.kit.datamanager.clients.SimpleServiceClient;
import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Utilities class for mapping files.
 */
public class MappingUtil {

    /**
     * Return codes.
     */
    public static final int SUCCESS = 0;
    public static final int FAILURE = Integer.MIN_VALUE;

    /**
     * Logger for this class.
     */
    private final static Logger LOG = LoggerFactory.getLogger(MappingUtil.class);

    private final ApplicationProperties configuration;

    @Autowired
    public MappingUtil(ApplicationProperties configuration) {
        this.configuration = configuration;
    }

    /**
     * Map the source file to a new file using a given mapping tool.
     *
     * @param mappingFile The absolute path to mapping file.
     * @param srcFile     The absolute path to the source file.
     * @param mapping     The absolute path to the mapping.
     * @return Errorcode (0 = SUCCESS)
     * @throws MappingException if an error occurs.
     */
    public Optional<Path> mapFile(Path mappingFile, Path srcFile, String mapping) {
        Path resultFile;
        int returnCode = FAILURE;
        resultFile = FileUtil.createTempFile(mapping + "_", ".mapping");
        try {
            returnCode = mapFile(mappingFile, srcFile, resultFile, mapping);
        } finally {
            if (returnCode != SUCCESS) {
                FileUtil.removeFile(resultFile);
                resultFile = null;
            }
        }

        return Optional.ofNullable(resultFile);
    }

    /**
     * Map the source file to a new file using a given mapping tool.
     *
     * @param mappingFile The absolute path to mapping file.
     * @param srcFile     The absolute path to the source file.
     * @param resultFile  The absolute path to the created mapping.
     * @return Errorcode (0 = SUCCESS)
     */
    public int mapFile(Path mappingFile, Path srcFile, Path resultFile, String mapping) {
        int returnValue;

        IMappingTool mappingTool = IMappingTool.getMappingTool(configuration, mapping);
        if (resultFile.toFile().exists() && ((resultFile.toFile().length() > 0) || !resultFile.toFile().canWrite())) {
            throw new MappingException("Overwriting file '" + resultFile + "' is not allowed!");
        }
        returnValue = mappingTool.mapFile(mappingFile, srcFile, resultFile);

        return returnValue;
    }
}
