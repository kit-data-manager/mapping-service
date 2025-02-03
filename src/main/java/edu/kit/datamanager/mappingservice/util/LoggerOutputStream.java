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

package edu.kit.datamanager.mappingservice.util;

import org.slf4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class for redirecting the output of a process to a logger.
 *
 * @author maximilianiKIT
 */
public class LoggerOutputStream extends OutputStream {

    private final Logger logger;

    /**
     * Available log levels.
     */
    public enum Level {TRACE, DEBUG, INFO, WARN, ERROR}

    private final Level level;

    /**
     * @param logger Logger to redirect the output to.
     * @param level  Level of the logger.
     */
    public LoggerOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) {
        String message = new String(b, off, len);
        if (!message.isEmpty() && !message.equals(" ")) {
            switch (level) {
                case TRACE:
                    logger.trace(message);
                    break;
                case DEBUG:
                    logger.debug(message);
                    break;
                case INFO:
                    logger.info(message);
                    break;
                case WARN:
                    logger.warn(message);
                    break;
                case ERROR:
                    logger.error(message);
                    break;
            }
        }
    }
}