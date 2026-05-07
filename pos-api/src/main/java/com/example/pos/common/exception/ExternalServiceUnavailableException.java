package com.example.pos.common.exception;

public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException() {
        super();
    }

    public ExternalServiceUnavailableException(String message) {
        super(message);
    }

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
