package edu.kit.datamanager.mappingservice.plugins;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public enum MappingPluginState implements Serializable {
    SUCCESS(HttpStatus.OK),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    TIMEOUT,
    EXECUTION_ERROR,
    INSUFFICIENT_PRIVILEGES(HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private HttpStatus httpStatus;

    public HttpStatus getHttpStatus(){
        return httpStatus;
    }

    MappingPluginState(HttpStatus status){
        this.httpStatus = status;
    }

    MappingPluginState(){
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
