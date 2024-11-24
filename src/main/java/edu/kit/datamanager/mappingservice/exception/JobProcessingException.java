/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.kit.datamanager.mappingservice.exception;

/**
 *
 * @author Torridity
 */
public class JobProcessingException extends Exception {

    private boolean badRequest = false;

    public JobProcessingException() {
        super();
    }

    public JobProcessingException(String message) {
        super(message);
    }

    public JobProcessingException(String message, boolean badRequest) {
        super(message);
        this.badRequest = badRequest;
    }

    public boolean isBadRequest() {
        return badRequest;
    }

}
