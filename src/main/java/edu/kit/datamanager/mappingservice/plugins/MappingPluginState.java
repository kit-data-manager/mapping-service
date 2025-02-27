package edu.kit.datamanager.mappingservice.plugins;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * State of a mapping plugin. This class is used to store the state of a mapping
 * plugin and return an HTTP status code.
 *
 * @author maximilianiKIT
 */
public class MappingPluginState implements Serializable {

    public enum StateEnum {
        SUCCESS(HttpStatus.OK),
        NOT_FOUND(HttpStatus.NOT_FOUND),
        TIMEOUT,
        EXECUTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
        INVALID_INPUT,
        BAD_EXIT_CODE,
        INCORRECT_MIME_TYPE,
        INSUFFICIENT_PRIVILEGES(HttpStatus.INTERNAL_SERVER_ERROR),
        UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

        private final HttpStatus httpStatus;

        StateEnum() {
            this.httpStatus = HttpStatus.BAD_REQUEST;
        }

        StateEnum(HttpStatus status) {
            this.httpStatus = status;
        }

        public HttpStatus getHttpStatus() {
            return httpStatus;
        }

    };

    private final StateEnum state;
    private Object details;

    /**
     * Lets the state be created with an HTTP status code.
     *
     * @param state The state enum.
     */
    public MappingPluginState(StateEnum state) {
        this.state = state;
    }

    public static MappingPluginState SUCCESS() {
        return new MappingPluginState(StateEnum.SUCCESS);
    }

    public static MappingPluginState NOT_FOUND() {
        return new MappingPluginState(StateEnum.NOT_FOUND);
    }

    public static MappingPluginState TIMEOUT() {
        return new MappingPluginState(StateEnum.TIMEOUT);
    }

    public static MappingPluginState EXECUTION_ERROR() {
        return new MappingPluginState(StateEnum.EXECUTION_ERROR);
    }

    public static MappingPluginState INVALID_INPUT() {
        return new MappingPluginState(StateEnum.INVALID_INPUT);
    }

    public static MappingPluginState BAD_EXIT_CODE() {
        return new MappingPluginState(StateEnum.BAD_EXIT_CODE);
    }

    public static MappingPluginState INCORRECT_MIME_TYPE() {
        return new MappingPluginState(StateEnum.INCORRECT_MIME_TYPE);
    }

    public static MappingPluginState INSUFFICIENT_PRIVILEGES() {
        return new MappingPluginState(StateEnum.INSUFFICIENT_PRIVILEGES);
    }

    public static MappingPluginState UNKNOWN_ERROR() {
        return new MappingPluginState(StateEnum.UNKNOWN_ERROR);
    }

    public StateEnum getState() {
        return state;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    public Object getDetails() {
        return details;
    }

}
