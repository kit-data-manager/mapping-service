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
package edu.kit.datamanager.mappingservice.configuration;

import edu.kit.datamanager.annotations.ExecutableFileURL;
import edu.kit.datamanager.annotations.LocalFolderURL;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URL;

/**
 * This class is used to configure the application. It reads the values from the
 * application.properties file.
 *
 * @author maximilianiKIT
 */
@ConfigurationProperties(prefix = "mapping-service")
@Component
@Data
@Validated
@EqualsAndHashCode
public class ApplicationProperties {

    /**
     * The absolute path to the python interpreter.
     */
    @ExecutableFileURL
    @Value("${mapping-service.pythonExecutable}")
    private URL pythonExecutable;

    /**
     * The absolute path where the plugins are stored.
     */
    @LocalFolderURL
    @Value("${mapping-service.pluginLocation}")
    private URL pluginLocation;

    /**
     * The absolute path where the mappings are stored.
     */
    @LocalFolderURL
    @Value("${mapping-service.mappingSchemasLocation}")
    private URL mappingsLocation;

    /**
     * The absolute path where job data is stored.
     */
    @LocalFolderURL
    @Value("${mapping-service.jobOutput}")
    private URL jobOutputLocation;

    /**
     * One or more packages to scan for plugin classes.
     */
    @Value("${mapping-service.packagesToScan:edu.kit.datamanager.mappingservice.plugins.impl}")
    private String[] packagesToScan;

    @Value("${mapping-service.executionTimeout:30}")
    private int executionTimeout;

    /**
     * Auth and permission properties
     */
    @Value("${mapping-service.authEnabled:FALSE}")
    private boolean authEnabled;
    @Value("${mapping-service.mappingAdminRole:MAPPING_ADMIN}")
    private String mappingAdminRole;
     /**
     * CORS and CSRF properties
     */
    @Value("${repo.security.allowedOriginPattern:*}")
    private String allowedOriginPattern;
    @Value("${repo.security.allowedMethods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String[] allowedMethods;
    @Value("${repo.security.exposedHeaders:Content-Range,ETag,Link}")
    private String[] exposedHeaders;
    @Value("${repo.security.allowedHeaders:*}")
    private String[] allowedHeaders;

}
