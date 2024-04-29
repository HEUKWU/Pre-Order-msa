package com.heukwu.common.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(ErrorMessage message) {
        super(message.getMessage());
    }
}
