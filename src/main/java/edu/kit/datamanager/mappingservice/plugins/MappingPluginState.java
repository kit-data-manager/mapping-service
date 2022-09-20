package edu.kit.datamanager.mappingservice.plugins;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * State of a mapping plugin.
 * This class is used to store the state of a mapping plugin and return an HTTP status code.
 *
 * @author maximilianiKIT
 */
public enum MappingPluginState implements Serializable {
    SUCCESS(HttpStatus.OK),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    TIMEOUT,
    EXECUTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT,
    INCORRECT_MIME_TYPE,
    INSUFFICIENT_PRIVILEGES(HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    /**
     * This method returns the HTTP status code for the given state.
     *
     * @return The HTTP status code for the given state.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Lets the state be created with an HTTP status code.
     *
     * @param status The HTTP status code for the given state.
     */
    MappingPluginState(HttpStatus status) {
        this.httpStatus = status;
    }

    /**
     * Default constructor.
     * Sets the default HTTP status code on error to 400.
     */
    MappingPluginState() {
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
