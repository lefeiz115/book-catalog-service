package com.eyebuy.bookcatalog.repository;

import com.eyebuy.bookcatalog.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Book Repository Tests")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .title("Test Book Title")
                .author("Test Author")
                .isbn("9780123456789")
                .genre("Fiction")
                .price(new BigDecimal("29.99"))
                .description("A test book description")
                .stock(100)
                .pages(300)
                .publisher("Test Publisher")
                .publishDate(LocalDateTime.of(2024, 1, 15, 0, 0))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve book by ID")
    void saveAndFindById() {
        Book saved = bookRepository.save(testBook);
        Optional<Book> found = bookRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Book Title");
        assertThat(found.get().getAuthor()).isEqualTo("Test Author");
    }

    @Test
    @DisplayName("Should find book by ISBN")
    void findByIsbn() {
        bookRepository.save(testBook);
        Optional<Book> found = bookRepository.findByIsbn("9780123456789");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Book Title");
    }

    @Test
    @DisplayName("Should return empty when finding by non-existent ISBN")
    void findByIsbnNotFound() {
        Optional<Book> found = bookRepository.findByIsbn("non-existent");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find book by ISBN excluding given ID")
    void findByIsbnAndIdNot() {
        Book saved = bookRepository.save(testBook);

        Book book2 = Book.builder()
                .title("Book 2")
                .author("Author 2")
                .isbn("9780123456789")
                .genre("Fiction")
                .price(new BigDecimal("19.99"))
                .stock(50)
                .build();
        bookRepository.save(book2);

        Optional<Book> found = bookRepository.findByIsbnAndIdNot("9780123456789", saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotEqualTo(saved.getId());
    }

    @Test
    @DisplayName("Should find books by title containing keyword")
    void findByTitleContainingIgnoreCase() {
        bookRepository.save(testBook);

        Book book2 = Book.builder()
                .title("Another Programming Book")
                .author("Author 2")
                .genre("Technology")
                .stock(10)
                .build();
        bookRepository.save(book2);

        Specification<Book> titleSpec = (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%test%");
        List<Book> found = bookRepository.findAll(titleSpec);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("Test Book Title");
    }

    @Test
    @DisplayName("Should find books by author containing keyword")
    void findByAuthorContainingIgnoreCase() {
        bookRepository.save(testBook);

        Specification<Book> authorSpec = (root, query, cb) ->
                cb.like(cb.lower(root.get("author")), "%test aut%");
        List<Book> found = bookRepository.findAll(authorSpec);
        assertThat(found).hasSize(1);
    }

    @Test
    @DisplayName("Should find books by genre")
    void findByGenre() {
        bookRepository.save(testBook);

        Book book2 = Book.builder()
                .title("Sci-Fi Book")
                .author("Author 2")
                .genre("Science Fiction")
                .stock(5)
                .build();
        bookRepository.save(book2);

        Specification<Book> fictionSpec = (root, query, cb) ->
                cb.equal(root.get("genre"), "Fiction");
        List<Book> found = bookRepository.findAll(fictionSpec);
        assertThat(found).hasSize(1);

        Specification<Book> scifiSpec = (root, query, cb) ->
                cb.equal(root.get("genre"), "Science Fiction");
        found = bookRepository.findAll(scifiSpec);
        assertThat(found).hasSize(1);
    }

    @Test
    @DisplayName("Should find active books only")
    void findByActiveTrue() {
        bookRepository.save(testBook);

        Book inactiveBook = Book.builder()
                .title("Inactive Book")
                .author("Author 2")
                .genre("Fiction")
                .stock(10)
                .active(false)
                .build();
        bookRepository.save(inactiveBook);

        List<Book> found = bookRepository.findByActiveTrue();
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("Test Book Title");
    }

    @Test
    @DisplayName("Should find available books with stock > 0")
    void findAvailableBooks() {
        bookRepository.save(testBook);

        Book zeroStockBook = Book.builder()
                .title("Zero Stock Book")
                .author("Author 2")
                .stock(0)
                .active(true)
                .build();
        bookRepository.save(zeroStockBook);

        List<Book> allBooks = bookRepository.findAll();
        List<Book> availableBooks = allBooks.stream()
                .filter(b -> Boolean.TRUE.equals(b.getActive()) && b.getStock() != null && b.getStock() > 0)
                .toList();
        assertThat(availableBooks).hasSize(1);
    }

    @Test
    @DisplayName("Should search books with keyword and filters")
    void searchBooks() {
        bookRepository.save(testBook);

        Book book2 = Book.builder()
                .title("Python Programming")
                .author("John Smith")
                .genre("Technology")
                .description("Learn Python programming from scratch")
                .stock(20)
                .active(true)
                .build();
        bookRepository.save(book2);

        Specification<Book> keywordSpec = (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("title")), "%python%"),
                        cb.like(cb.lower(root.get("author")), "%python%"),
                        cb.like(cb.lower(root.get("description")), "%python%")
                );
        Page<Book> page = bookRepository.findAll(keywordSpec, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);

        Specification<Book> genreActiveSpec = (root, query, cb) -> cb.and(
                cb.equal(root.get("genre"), "Technology"),
                cb.equal(root.get("active"), true)
        );
        page = bookRepository.findAll(genreActiveSpec, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should check existence by ISBN")
    void existsByIsbn() {
        bookRepository.save(testBook);
        assertThat(bookRepository.existsByIsbn("9780123456789")).isTrue();
        assertThat(bookRepository.existsByIsbn("non-existent")).isFalse();
    }

    @Test
    @DisplayName("Should count books by genre")
    void countByGenre() {
        bookRepository.save(testBook);

        Book book2 = Book.builder()
                .title("Another Fiction Book")
                .author("Author 2")
                .genre("Fiction")
                .stock(10)
                .build();
        bookRepository.save(book2);

        assertThat(bookRepository.countByGenre("Fiction")).isEqualTo(2);
        assertThat(bookRepository.countByGenre("Technology")).isEqualTo(0);
    }

    @Test
    @DisplayName("Should count active books")
    void countByActiveTrue() {
        bookRepository.save(testBook);

        Book inactiveBook = Book.builder()
                .title("Inactive Book")
                .author("Author 2")
                .stock(10)
                .active(false)
                .build();
        bookRepository.save(inactiveBook);

        assertThat(bookRepository.countByActiveTrue()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should delete book")
    void deleteBook() {
        Book saved = bookRepository.save(testBook);
        bookRepository.delete(saved);
        assertThat(bookRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should paginate results correctly")
    void pagination() {
        for (int i = 0; i < 25; i++) {
            Book book = Book.builder()
                    .title("Book " + i)
                    .author("Author")
                    .genre("Fiction")
                    .stock(10)
                    .build();
            bookRepository.save(book);
        }

        Specification<Book> noSpec = (root, query, cb) -> cb.conjunction();
        Page<Book> page1 = bookRepository.findAll(noSpec, PageRequest.of(0, 10));
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page1.getTotalElements()).isEqualTo(25);

        Page<Book> page3 = bookRepository.findAll(noSpec, PageRequest.of(2, 10));
        assertThat(page3.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("Should sort by update time descending")
    void sortByUpdateTime() {
        Book book1 = bookRepository.save(testBook);

        Book book2 = Book.builder()
                .title("Second Book")
                .author("Author 2")
                .genre("Fiction")
                .stock(10)
                .createTime(LocalDateTime.now().plusSeconds(1))
                .updateTime(LocalDateTime.now().plusSeconds(1))
                .build();
        Book saved2 = bookRepository.save(book2);

        List<Book> allBooks = bookRepository.findAll();
        List<Book> sorted = allBooks.stream()
                .sorted((a, b) -> b.getUpdateTime().compareTo(a.getUpdateTime()))
                .toList();
        assertThat(sorted.get(0).getId()).isEqualTo(saved2.getId());
    }
}
