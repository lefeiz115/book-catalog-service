package com.eyebuy.bookcatalog.service.strategy;

import com.eyebuy.bookcatalog.service.strategy.AuthorSearchStrategy;
import com.eyebuy.bookcatalog.service.strategy.GenreSearchStrategy;
import com.eyebuy.bookcatalog.service.strategy.TitleSearchStrategy;
import com.eyebuy.bookcatalog.entity.Book;
import com.eyebuy.bookcatalog.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Search Strategy Tests")
class SearchStrategyTest {

    @Mock
    private BookRepository bookRepository;

    @Test
    @DisplayName("Title search strategy should find books by title")
    void titleSearchStrategy() {
        TitleSearchStrategy strategy = new TitleSearchStrategy(bookRepository);

        Book book = Book.builder()
                .title("Java Programming")
                .author("Author A")
                .stock(10)
                .build();
        when(bookRepository.findByTitleContainingIgnoreCase("java")).thenReturn(Arrays.asList(book));

        List<Book> results = strategy.execute("java");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Java Programming");
        assertThat(strategy.getName()).isEqualTo("title");
    }

    @Test
    @DisplayName("Author search strategy should find books by author")
    void authorSearchStrategy() {
        AuthorSearchStrategy strategy = new AuthorSearchStrategy(bookRepository);

        Book book = Book.builder()
                .title("Book A")
                .author("Author Smith")
                .stock(5)
                .build();
        when(bookRepository.findByAuthorContainingIgnoreCase("smith")).thenReturn(Arrays.asList(book));

        List<Book> results = strategy.execute("smith");
        assertThat(results).hasSize(1);
        assertThat(strategy.getName()).isEqualTo("author");
    }

    @Test
    @DisplayName("Genre search strategy should find books by genre")
    void genreSearchStrategy() {
        GenreSearchStrategy strategy = new GenreSearchStrategy(bookRepository);

        Book book = Book.builder()
                .title("Sci-Fi Book")
                .author("Author B")
                .genre("Science Fiction")
                .stock(3)
                .build();
        when(bookRepository.findByGenre("Science Fiction")).thenReturn(Arrays.asList(book));

        List<Book> results = strategy.execute("Science Fiction");
        assertThat(results).hasSize(1);
        assertThat(strategy.getName()).isEqualTo("genre");
    }

    @Test
    @DisplayName("Title search strategy should return empty for no match")
    void titleSearchNoMatch() {
        TitleSearchStrategy strategy = new TitleSearchStrategy(bookRepository);
        when(bookRepository.findByTitleContainingIgnoreCase("nonexistent")).thenReturn(List.of());

        List<Book> results = strategy.execute("nonexistent");
        assertThat(results).isEmpty();
    }
}
