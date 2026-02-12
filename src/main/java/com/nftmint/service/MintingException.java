package com.nftmint.service;

public class MintingException extends RuntimeException {
    private final ErrorType errorType;

    public enum ErrorType {
        SYSTEM_ERROR,
        INVALID_REQUEST,
        CONCURRENCY_FAIL
    }
    
    public MintingException(String message) {
        super(message);
        this.errorType = ErrorType.SYSTEM_ERROR;
    }

    public MintingException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}