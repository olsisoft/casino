package com.casino.user.exception;

public class CashbackException extends RuntimeException {
    public CashbackException(String message) {
        super(message);
    }

    public CashbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
