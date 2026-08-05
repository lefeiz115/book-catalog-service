package com.eyebuy.bookcatalog.service.strategy;

import com.eyebuy.bookcatalog.entity.Book;
import com.eyebuy.bookcatalog.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("title")
@RequiredArgsConstructor
public class TitleSearchStrategy implements BookSearchStrategy {

    private final BookRepository bookRepository;

    @Override
    public String getName() {
        return "title";
    }

    @Override
    public List<Book> execute(String value) {
        return bookRepository.findByTitleContainingIgnoreCase(value);
    }
}
