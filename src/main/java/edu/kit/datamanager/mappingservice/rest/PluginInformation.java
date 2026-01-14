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
package edu.kit.datamanager.mappingservice.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.kit.datamanager.mappingservice.plugins.IMappingPlugin;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class for displaying information about a plugin.
 *
 * @author maximilianiKIT
 */
@JsonSerialize
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class PluginInformation implements Serializable {

    /**
     * ID of the plugin.
     */
    @Id
    String id;

    /**
     * Name of the plugin.
     */
    String name;

    /**
     * Version of the plugin.
     */
    String version;

    /**
     * Description of the plugin.
     */
    String description;

    /**
     * URI to the plugin, the plugins author, ... or the tool which is used by
     * the plugin.
     */
    String uri;

    /**
     * The mime types of the input data.
     */
    String[] inputTypes;

    /**
     * The mime types of the output data.
     */
    String[] outputTypes;

    /**
     * Constructor for creating a PluginInformation object by getting these
     * information from the plugins themselves via the PluginManager.
     */
    public PluginInformation(String id, PluginManager manager) throws MappingPluginException {
        this.id = id;
        IMappingPlugin p = manager.getPlugins().get(id);
        if (p != null) {
            this.name = p.name();
            this.version = p.version();
            this.description = p.description();
            this.uri = p.uri();
            ArrayList<String> inputTypesList = new ArrayList<>(Arrays.stream(p.inputTypes()).toList());
            this.inputTypes = inputTypesList.toArray(String[]::new);
            ArrayList<String> outputTypesList = new ArrayList<>(Arrays.stream(p.outputTypes()).toList());
            this.outputTypes = outputTypesList.toArray(String[]::new);
        } else {
            throw new MappingPluginException(MappingPluginState.NOT_FOUND(), "Plugin with id " + id + " not found.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        PluginInformation that = (PluginInformation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
