package com.casino.user.exception;

public class VipException extends RuntimeException {
    public VipException(String message) {
        super(message);
    }

    public VipException(String message, Throwable cause) {
        super(message, cause);
    }
}
