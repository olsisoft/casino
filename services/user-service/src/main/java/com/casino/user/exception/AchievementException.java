package com.casino.user.exception;

public class AchievementException extends RuntimeException {
    public AchievementException(String message) {
        super(message);
    }

    public AchievementException(String message, Throwable cause) {
        super(message, cause);
    }
}
