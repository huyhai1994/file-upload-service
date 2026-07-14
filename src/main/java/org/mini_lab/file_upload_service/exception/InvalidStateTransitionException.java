package org.mini_lab.file_upload_service.exception;

public class InvalidStateTransitionException extends RuntimeException{

    public InvalidStateTransitionException() {
        super("Invalid State Transition");
    }
}
