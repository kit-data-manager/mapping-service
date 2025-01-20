/*
 * Copyright 2022 Karlsruhe Institute of Technology.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kit.datamanager.mappingservice.plugins;

import org.springframework.util.MimeType;

import java.nio.file.Path;

/**
 * Interface for mapping plugins.
 * Every plugin which implements this interface and is placed in the plugins folder will be loaded and usable via the REST-API.
 *
 * @author maximilianiKIT
 */
public interface IMappingPlugin {

    /**
     * Name of the plugin which gets displayed in the UI and is part of the id.
     *
     * @return The name of the plugin.
     */
    String name();

    /**
     * A short description of the plugin which gets displayed in the UI.
     *
     * @return The description of the plugin.
     */
    String description();

    /**
     * The version of the plugin which gets displayed in the UI and is part of the id.
     *
     * @return The version of the plugin.
     */
    String version();

    /**
     * A URI which refers to the plugin or the technology used by the plugin (e.g. a link to a GitHub repository).
     * This URI will be displayed in the UI.
     *
     * @return The URI of the plugin.
     */
    String uri();

    /**
     * The mime type of the input data.
     *
     * @return The mime type of the input data.
     */
    MimeType[] inputTypes();

    /**
     * The mime type of the output data.
     *
     * @return The mime type of the output data.
     */
    MimeType[] outputTypes();

    /**
     * The id of the plugin which is used to identify the plugin.
     * By default, the id is composed of the name and the version of the plugin (e.g. testPlugin_2.1.0).
     *
     * @return The id of the plugin.
     */
    default String id() {
        return name() + "_" + version();
    }

    /**
     * This method is called when the plugin is loaded.
     * It can be used to initialize the plugin and install dependencies.
     */
    void setup();

    /**
     * The method which is called to execute the plugin.
     *
     * @param inputFile   The path to the output document.
     * @param outputFile  The path to the output document.
     * @param mappingFile The path to the mapping schema.
     * @return The exit code of the plugin.
     * 
     * @throws MappingPluginException If the mapping execution fails.
     */
    MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException;
}
