package org.mini_lab.file_upload_service.security.notification.exception;

import org.springframework.http.HttpStatusCode;

public class NotificationServiceException extends RuntimeException {
    public NotificationServiceException(HttpStatusCode code) {
        super("Error status code " + code);
    }
}
