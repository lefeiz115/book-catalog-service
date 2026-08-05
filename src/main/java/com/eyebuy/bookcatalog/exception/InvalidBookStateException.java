package com.eyebuy.bookcatalog.exception;

public class InvalidBookStateException extends RuntimeException {

    public InvalidBookStateException(String message) {
        super(message);
    }

    public InvalidBookStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
