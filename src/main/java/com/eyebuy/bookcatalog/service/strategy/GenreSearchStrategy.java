package com.eyebuy.bookcatalog.service.strategy;

import com.eyebuy.bookcatalog.entity.Book;
import com.eyebuy.bookcatalog.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("genre")
@RequiredArgsConstructor
public class GenreSearchStrategy implements BookSearchStrategy {

    private final BookRepository bookRepository;

    @Override
    public String getName() {
        return "genre";
    }

    @Override
    public List<Book> execute(String value) {
        return bookRepository.findByGenre(value);
    }
}
