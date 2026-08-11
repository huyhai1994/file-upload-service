package org.mini_lab.file_upload_service.file_upload.shared.exception;

public class InvalidStateTransitionException extends RuntimeException{

    public InvalidStateTransitionException() {
        super("Invalid State Transition");
    }
}
