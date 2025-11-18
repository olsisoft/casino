package com.casino.user.exception;

public class BonusException extends RuntimeException {
    public BonusException(String message) {
        super(message);
    }

    public BonusException(String message, Throwable cause) {
        super(message, cause);
    }
}
