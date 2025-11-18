package com.casino.user.exception;

public class DailyRewardException extends RuntimeException {
    public DailyRewardException(String message) {
        super(message);
    }

    public DailyRewardException(String message, Throwable cause) {
        super(message, cause);
    }
}
