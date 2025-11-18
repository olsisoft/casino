package com.casino.user.exception;

public class AffiliateException extends RuntimeException {
    public AffiliateException(String message) {
        super(message);
    }

    public AffiliateException(String message, Throwable cause) {
        super(message, cause);
    }
}
