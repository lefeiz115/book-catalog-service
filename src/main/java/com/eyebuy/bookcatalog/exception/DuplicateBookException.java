package com.eyebuy.bookcatalog.exception;

import lombok.Getter;

@Getter
public class DuplicateBookException extends RuntimeException {

    private final String fieldName;
    private final String fieldValue;

    public DuplicateBookException(String fieldName, String fieldValue) {
        super(String.format("Book with %s '%s' already exists", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
