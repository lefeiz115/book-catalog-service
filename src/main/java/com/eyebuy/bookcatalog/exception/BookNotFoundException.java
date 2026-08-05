package com.eyebuy.bookcatalog.exception;

import lombok.Getter;

@Getter
public class BookNotFoundException extends RuntimeException {

    private final Long bookId;

    public BookNotFoundException(Long bookId) {
        super("Book not found with id: " + bookId);
        this.bookId = bookId;
    }

    public BookNotFoundException(Long bookId, String message) {
        super(message);
        this.bookId = bookId;
    }
}
