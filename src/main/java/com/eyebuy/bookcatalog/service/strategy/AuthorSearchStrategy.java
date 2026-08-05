package com.eyebuy.bookcatalog.service.strategy;

import com.eyebuy.bookcatalog.entity.Book;
import com.eyebuy.bookcatalog.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("author")
@RequiredArgsConstructor
public class AuthorSearchStrategy implements BookSearchStrategy {

    private final BookRepository bookRepository;

    @Override
    public String getName() {
        return "author";
    }

    @Override
    public List<Book> execute(String value) {
        return bookRepository.findByAuthorContainingIgnoreCase(value);
    }
}
