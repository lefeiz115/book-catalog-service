package com.eyebuy.bookcatalog.service.strategy;

import com.eyebuy.bookcatalog.entity.Book;

import java.util.List;

public interface BookSearchStrategy {
    String getName();
    List<Book> execute(String value);
}
