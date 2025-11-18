package com.casino.user.exception;

public class PromoCodeException extends RuntimeException {
    public PromoCodeException(String message) {
        super(message);
    }

    public PromoCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
