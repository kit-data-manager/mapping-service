package edu.kit.datamanager.mappingservice.plugins;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * State of a mapping plugin. This class is used to store the state of a mapping
 * plugin and return an HTTP status code.
 *
 * @author maximilianiKIT
 */
@Getter
public class MappingPluginState implements Serializable {

    @Getter
    public enum StateEnum {
        SUCCESS(HttpStatus.OK),
        NOT_FOUND(HttpStatus.NOT_FOUND),
        TIMEOUT(HttpStatus.GATEWAY_TIMEOUT),
        EXECUTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
        INVALID_INPUT(HttpStatus.BAD_REQUEST),
        BAD_EXIT_CODE(HttpStatus.INTERNAL_SERVER_ERROR),
        UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

        private final HttpStatus httpStatus;

        StateEnum(HttpStatus status) {
            this.httpStatus = status;
        }
    };

    private final StateEnum state;
    @Setter
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

    public static MappingPluginState UNKNOWN_ERROR() {
        return new MappingPluginState(StateEnum.UNKNOWN_ERROR);
    }
}
