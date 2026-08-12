package org.mini_lab.file_upload_service.security.authentication.login.exception;

public class UserAccountLockedException extends RuntimeException {
    public UserAccountLockedException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public UserAccountLockedException() {

    }
}
