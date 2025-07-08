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
package edu.kit.datamanager.mappingservice.plugins;

import lombok.Getter;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown by mapping plugins.
 *
 * @author maximilianiKIT
 */
@Getter
public class MappingPluginException extends Exception {

    /**
     * State of the plugin.
     */
    private final MappingPluginState mappingPluginState;

    /**
     * Default constructor.
     *
     * @param state State of the plugin.
     */
    public MappingPluginException(MappingPluginState state) {
        super(state.getState().name());
        this.mappingPluginState = state;
    }

    /**
     * Default constructor.
     *
     * @param state State of the plugin.
     * @param message More detailed message which gets returned via REST.
     */
    public MappingPluginException(MappingPluginState state, String message) {
        super(message);
        this.mappingPluginState = state;
    }

    /**
     * Default constructor.
     *
     * @param state State of the plugin.
     * @param message More detailed message which gets returned via REST.
     * @param cause Cause of the exception.
     */
    public MappingPluginException(MappingPluginState state, String message, Throwable cause) {
        super(message, cause);
        this.mappingPluginState = state;
    }

    public void throwMe() throws ResponseStatusException {
        throw new ResponseStatusException(this.mappingPluginState.getState().getHttpStatus(), "Cause: " + this.mappingPluginState.getDetails());
    }
}
