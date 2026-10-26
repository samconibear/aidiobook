package com.audiobook.shared.exception;

public class AudiobookException extends RuntimeException {
    public AudiobookException(String message) {
        super(message);
    }

    public AudiobookException(String message, Throwable cause) {
        super(message, cause);
    }
}
