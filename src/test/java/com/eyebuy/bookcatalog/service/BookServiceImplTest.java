package com.eyebuy.bookcatalog.service;

import com.eyebuy.bookcatalog.dto.*;
import com.eyebuy.bookcatalog.service.impl.BookServiceImpl;
import com.eyebuy.bookcatalog.service.template.CsvBookExporter;
import com.eyebuy.bookcatalog.service.template.JsonBookExporter;
import com.eyebuy.bookcatalog.entity.Book;
import com.eyebuy.bookcatalog.event.BookEvent;
import com.eyebuy.bookcatalog.event.EventPublisher;
import com.eyebuy.bookcatalog.exception.BookNotFoundException;
import com.eyebuy.bookcatalog.exception.DuplicateBookException;
import com.eyebuy.bookcatalog.exception.InvalidBookStateException;
import com.eyebuy.bookcatalog.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Book Service Implementation Tests")
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private CsvBookExporter csvExporter;

    @Mock
    private JsonBookExporter jsonExporter;

    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookRepository, eventPublisher, csvExporter, jsonExporter);
    }

    private Book createTestBook(Long id, String title, String author, String isbn, String genre,
                                 BigDecimal price, Integer stock, Boolean active) {
        Book book = Book.builder()
                .id(id)
                .title(title)
                .author(author)
                .isbn(isbn)
                .genre(genre)
                .price(price)
                .description("Test description for " + title)
                .stock(stock)
                .pages(300)
                .publisher("Test Publisher")
                .publishDate(LocalDateTime.of(2024, 1, 15, 0, 0))
                .active(active)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        return book;
    }

    @Nested
    @DisplayName("Create Book Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully")
        void createBookSuccessfully() {
            BookCreateRequest request = BookCreateRequest.builder()
                    .title("New Book")
                    .author("New Author")
                    .isbn("1234567890123")
                    .genre("Fiction")
                    .price(new BigDecimal("19.99"))
                    .stock(50)
                    .pages(250)
                    .publisher("Test Publisher")
                    .build();

            Book savedBook = createTestBook(1L, "New Book", "New Author", "1234567890123",
                    "Fiction", new BigDecimal("19.99"), 50, true);

            when(bookRepository.existsByIsbn("1234567890123")).thenReturn(false);
            when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

            BookResponse response = bookService.createBook(request);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("New Book");
            assertThat(response.getAuthor()).isEqualTo("New Author");
            assertThat(response.getStock()).isEqualTo(50);
            verify(eventPublisher).publishBookCreated(any(), any(BookEvent.BookData.class));
        }

        @Test
        @DisplayName("Should throw DuplicateBookException when ISBN exists")
        void createBookWithDuplicateIsbn() {
            BookCreateRequest request = BookCreateRequest.builder()
                    .title("New Book")
                    .author("New Author")
                    .isbn("1234567890123")
                    .build();

            when(bookRepository.existsByIsbn("1234567890123")).thenReturn(true);

            assertThatThrownBy(() -> bookService.createBook(request))
                    .isInstanceOf(DuplicateBookException.class)
                    .hasMessageContaining("1234567890123");
        }

        @Test
        @DisplayName("Should create book with default values")
        void createBookWithDefaultValues() {
            BookCreateRequest request = BookCreateRequest.builder()
                    .title("Minimal Book")
                    .author("Minimal Author")
                    .build();

            Book savedBook = Book.builder()
                    .id(1L)
                    .title("Minimal Book")
                    .author("Minimal Author")
                    .stock(0)
                    .pages(0)
                    .active(true)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

            BookResponse response = bookService.createBook(request);

            assertThat(response.getStock()).isEqualTo(0);
            assertThat(response.getPages()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Get Book Tests")
    class GetBookTests {

        @Test
        @DisplayName("Should get book by ID successfully")
        void getBookByIdSuccessfully() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", new BigDecimal("29.99"), 100, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

            BookResponse response = bookService.getBookById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("Test Book");
        }

        @Test
        @DisplayName("Should throw BookNotFoundException when book not found")
        void getBookByIdNotFound() {
            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.getBookById(99L))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Update Book Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book successfully")
        void updateBookSuccessfully() {
            Book existingBook = createTestBook(1L, "Old Title", "Old Author", "1234567890123",
                    "Fiction", new BigDecimal("29.99"), 100, true);

            BookUpdateRequest request = BookUpdateRequest.builder()
                    .title("New Title")
                    .price(new BigDecimal("39.99"))
                    .build();

            Book updatedBook = createTestBook(1L, "New Title", "Old Author", "1234567890123",
                    "Fiction", new BigDecimal("39.99"), 100, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

            BookResponse response = bookService.updateBook(1L, request);

            assertThat(response.getTitle()).isEqualTo("New Title");
            assertThat(response.getPrice()).isEqualByComparingTo("39.99");
            verify(eventPublisher).publishBookUpdated(any(), any(BookEvent.BookData.class));
        }

        @Test
        @DisplayName("Should throw BookNotFoundException when updating non-existent book")
        void updateBookNotFound() {
            BookUpdateRequest request = BookUpdateRequest.builder()
                    .title("New Title")
                    .build();

            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.updateBook(99L, request))
                    .isInstanceOf(BookNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw DuplicateBookException when updating to existing ISBN")
        void updateBookWithDuplicateIsbn() {
            Book existingBook = createTestBook(1L, "Title", "Author", "1234567890123",
                    "Fiction", null, 100, true);

            BookUpdateRequest request = BookUpdateRequest.builder()
                    .isbn("9780123456789")
                    .build();

            when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
            when(bookRepository.findByIsbnAndIdNot("9780123456789", 1L))
                    .thenReturn(Optional.of(createTestBook(2L, "Other", "Author", "9780123456789",
                            "Fiction", null, 50, true)));

            assertThatThrownBy(() -> bookService.updateBook(1L, request))
                    .isInstanceOf(DuplicateBookException.class);
        }

        @Test
        @DisplayName("Should update book with all fields")
        void updateBookWithAllFields() {
            Book existingBook = createTestBook(1L, "Old Title", "Old Author", "1234567890123",
                    "Fiction", new BigDecimal("29.99"), 100, true);

            BookUpdateRequest request = BookUpdateRequest.builder()
                    .title("New Title")
                    .author("New Author")
                    .isbn("789-012")
                    .genre("Technology")
                    .price(new BigDecimal("49.99"))
                    .description("New description")
                    .stock(200)
                    .pages(400)
                    .publisher("New Publisher")
                    .publishDate("2024-06-15")
                    .active(false)
                    .build();

            Book updatedBook = createTestBook(1L, "New Title", "New Author", "789-012",
                    "Technology", new BigDecimal("49.99"), 200, false);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

            BookResponse response = bookService.updateBook(1L, request);

            assertThat(response.getTitle()).isEqualTo("New Title");
            assertThat(response.getAuthor()).isEqualTo("New Author");
        }
    }

    @Nested
    @DisplayName("Delete Book Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book successfully")
        void deleteBookSuccessfully() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", null, 0, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            doNothing().when(bookRepository).delete(book);

            bookService.deleteBook(1L);

            verify(bookRepository).delete(book);
            verify(eventPublisher).publishBookDeleted(any(), any(BookEvent.BookData.class));
        }

        @Test
        @DisplayName("Should throw BookNotFoundException when deleting non-existent book")
        void deleteBookNotFound() {
            when(bookRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.deleteBook(99L))
                    .isInstanceOf(BookNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw InvalidBookStateException when book has stock")
        void deleteBookWithStock() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", null, 50, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

            assertThatThrownBy(() -> bookService.deleteBook(1L))
                    .isInstanceOf(InvalidBookStateException.class)
                    .hasMessageContaining("stock");
        }
    }

    @Nested
    @DisplayName("Search Books Tests")
    class SearchBooksTests {

        @Test
        @DisplayName("Should search books with pagination")
        void searchBooksWithPagination() {
            BookSearchRequest request = BookSearchRequest.builder()
                    .keyword("test")
                    .page(0)
                    .size(10)
                    .build();

            List<Book> books = Arrays.asList(
                    createTestBook(1L, "Test Book 1", "Author 1", "111", "Fiction", null, 10, true),
                    createTestBook(2L, "Test Book 2", "Author 2", "2222222222222", "Fiction", null, 20, true)
            );

            Page<Book> page = new PageImpl<>(
                    books, PageRequest.of(0, 10), 2);

            when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<BookResponse> response = bookService.searchBooks(request);

            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should search books with default pagination")
        void searchBooksDefaultPagination() {
            BookSearchRequest request = BookSearchRequest.builder().build();

            Page<Book> page = new PageImpl<>(
                    Collections.emptyList(), PageRequest.of(0, 10), 0);

            when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<BookResponse> response = bookService.searchBooks(request);

            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Stock Update Tests")
    class StockUpdateTests {

        @Test
        @DisplayName("Should update stock successfully")
        void updateStockSuccessfully() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", null, 100, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(bookRepository.save(any(Book.class))).thenReturn(book);

            BookResponse response = bookService.updateStock(1L, -10);

            assertThat(response.getStock()).isEqualTo(90);
            verify(eventPublisher).publishStockChanged(any(), any(BookEvent.BookData.class), anyString());
        }

        @Test
        @DisplayName("Should throw InvalidBookStateException for negative stock")
        void updateStockNegative() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", null, 5, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

            assertThatThrownBy(() -> bookService.updateStock(1L, -10))
                    .isInstanceOf(InvalidBookStateException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should add stock successfully")
        void addStockSuccessfully() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", null, 50, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(bookRepository.save(any(Book.class))).thenReturn(book);

            BookResponse response = bookService.updateStock(1L, 50);

            assertThat(response.getStock()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should update stock from null stock")
        void updateStockFromNull() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", null, null, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(bookRepository.save(any(Book.class))).thenReturn(book);

            BookResponse response = bookService.updateStock(1L, 10);

            assertThat(response.getStock()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Toggle Active Tests")
    class ToggleActiveTests {

        @Test
        @DisplayName("Should toggle active to inactive")
        void toggleActiveToInactive() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", null, 100, true);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(bookRepository.save(any(Book.class))).thenReturn(book);

            BookResponse response = bookService.toggleActive(1L);

            assertThat(response.getActive()).isFalse();
            verify(eventPublisher).publishStatusChanged(any(), any(BookEvent.BookData.class), anyString());
        }

        @Test
        @DisplayName("Should toggle inactive to active")
        void toggleInactiveToActive() {
            Book book = createTestBook(1L, "Test Book", "Test Author", "1234567890123",
                    "Fiction", null, 100, false);

            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(bookRepository.save(any(Book.class))).thenReturn(book);

            BookResponse response = bookService.toggleActive(1L);

            assertThat(response.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Export Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export books as CSV")
        void exportBooksAsCsv() {
            List<Book> books = Arrays.asList(
                    createTestBook(1L, "Book 1", "Author 1", "111", "Fiction",
                            new BigDecimal("29.99"), 100, true),
                    createTestBook(2L, "Book 2", "Author 2", "2222222222222", "Technology",
                            new BigDecimal("39.99"), 50, true)
            );

            when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any())).thenReturn(books);

            ExportResult result = ExportResult.builder()
                    .fileName("test.csv")
                    .contentType("text/csv")
                    .content("Title,Author\nBook 1,Author 1".getBytes())
                    .recordCount(2)
                    .build();

            when(csvExporter.export(anyList())).thenReturn(result);

            BookSearchRequest request = BookSearchRequest.builder().build();
            ExportResult exportResult = bookService.exportBooks(request, "csv");

            assertThat(exportResult).isNotNull();
            assertThat(exportResult.getRecordCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should export books as JSON")
        void exportBooksAsJson() {
            List<Book> books = Collections.singletonList(
                    createTestBook(1L, "Book 1", "Author 1", "111", "Fiction",
                            new BigDecimal("29.99"), 100, true)
            );

            when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any())).thenReturn(books);

            ExportResult result = ExportResult.builder()
                    .fileName("test.json")
                    .contentType("application/json")
                    .content("[{\"title\":\"Book 1\"}]".getBytes())
                    .recordCount(1)
                    .build();

            when(jsonExporter.export(anyList())).thenReturn(result);

            BookSearchRequest request = BookSearchRequest.builder().build();
            ExportResult exportResult = bookService.exportBooks(request, "json");

            assertThat(exportResult.getContentType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("Should throw exception for unsupported format")
        void exportBooksUnsupportedFormat() {
            BookSearchRequest request = BookSearchRequest.builder().build();

            assertThatThrownBy(() -> bookService.exportBooks(request, "pdf"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported export format");
        }

        @Test
        @DisplayName("Should default to CSV when format is null")
        void exportBooksDefaultFormat() {
            List<Book> books = Collections.singletonList(
                    createTestBook(1L, "Book 1", "Author 1", "111", "Fiction",
                            null, 100, true)
            );

            when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any())).thenReturn(books);

            ExportResult result = ExportResult.builder()
                    .fileName("test.csv")
                    .contentType("text/csv")
                    .content("csv".getBytes())
                    .recordCount(1)
                    .build();

            when(csvExporter.export(anyList())).thenReturn(result);

            BookSearchRequest request = BookSearchRequest.builder().build();
            ExportResult exportResult = bookService.exportBooks(request, null);

            assertThat(exportResult.getContentType()).isEqualTo("text/csv");
        }
    }

    @Nested
    @DisplayName("AI Smart Search Tests")
    class AISmartSearchTests {

        @Test
        @DisplayName("Should return empty list for empty query")
        void aiSmartSearchEmptyQuery() {
            List<BookResponse> results = bookService.aiSmartSearch("");
            assertThat(results).isEmpty();

            results = bookService.aiSmartSearch(null);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should search by title relevance")
        void aiSmartSearchByTitle() {
            Book book = createTestBook(1L, "Python Programming Masterclass", "John Smith",
                    "111", "Technology", null, 100, true);
            Book book2 = createTestBook(2L, "Java Programming Masterclass", "Jane Doe",
                    "2222222222222", "Technology", null, 50, true);

            when(bookRepository.findByActiveTrue()).thenReturn(Arrays.asList(book, book2));

            List<BookResponse> results = bookService.aiSmartSearch("python programming");

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getTitle()).isEqualTo("Python Programming Masterclass");
        }

        @Test
        @DisplayName("Should search by author relevance")
        void aiSmartSearchByAuthor() {
            Book book = createTestBook(1L, "Book 1", "John Smith",
                    "111", "Fiction", null, 100, true);
            Book book2 = createTestBook(2L, "Book 2", "Jane Doe",
                    "2222222222222", "Fiction", null, 50, true);

            when(bookRepository.findByActiveTrue()).thenReturn(Arrays.asList(book, book2));

            List<BookResponse> results = bookService.aiSmartSearch("john smith");

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getAuthor()).isEqualTo("John Smith");
        }

        @Test
        @DisplayName("Should search by description relevance")
        void aiSmartSearchByDescription() {
            Book book = Book.builder()
                    .id(1L)
                    .title("Book 1")
                    .author("Author 1")
                    .description("This book covers artificial intelligence and machine learning")
                    .active(true)
                    .stock(100)
                    .build();

            when(bookRepository.findByActiveTrue()).thenReturn(Collections.singletonList(book));

            List<BookResponse> results = bookService.aiSmartSearch("artificial intelligence");

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty when no matches")
        void aiSmartSearchNoMatches() {
            Book book = createTestBook(1L, "Book 1", "Author 1",
                    "111", "Fiction", null, 100, true);

            when(bookRepository.findByActiveTrue()).thenReturn(Collections.singletonList(book));

            List<BookResponse> results = bookService.aiSmartSearch("quantum physics");

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get All Books Tests")
    class GetAllBooksTests {

        @Test
        @DisplayName("Should get all books")
        void getAllBooks() {
            List<Book> books = Arrays.asList(
                    createTestBook(1L, "Book 1", "Author 1", "111", "Fiction", null, 100, true),
                    createTestBook(2L, "Book 2", "Author 2", "2222222222222", "Fiction", null, 50, true)
            );
            Page<Book> page = new PageImpl<>(books);

            when(bookRepository.findAll(any(Pageable.class))).thenReturn(page);

            List<BookResponse> responses = bookService.getAllBooks();

            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no books")
        void getAllBooksEmpty() {
            Page<Book> emptyPage = new PageImpl<>(Collections.emptyList());

            when(bookRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            List<BookResponse> responses = bookService.getAllBooks();

            assertThat(responses).isEmpty();
        }
    }
}
