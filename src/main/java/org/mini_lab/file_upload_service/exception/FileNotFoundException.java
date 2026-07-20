package org.mini_lab.file_upload_service.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
        super("File not found!");
    }

}
