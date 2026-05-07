package com.example.pos.common.exception;

public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException() {
        super();
    }

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
