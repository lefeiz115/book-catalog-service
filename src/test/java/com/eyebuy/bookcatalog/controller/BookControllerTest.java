package com.eyebuy.bookcatalog.controller;

import com.eyebuy.bookcatalog.dto.*;
import com.eyebuy.bookcatalog.service.BookService;
import com.eyebuy.bookcatalog.exception.BookNotFoundException;
import com.eyebuy.bookcatalog.exception.DuplicateBookException;
import com.eyebuy.bookcatalog.exception.InvalidBookStateException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentMatchers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@DisplayName("Book Controller Integration Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookResponse createMockBookResponse(Long id, String title, String author) {
        return BookResponse.builder()
                .id(id)
                .title(title)
                .author(author)
                .isbn("1234567890123")
                .genre("Fiction")
                .price(new BigDecimal("29.99"))
                .description("Test description")
                .stock(100)
                .pages(300)
                .publisher("Test Publisher")
                .publishDate("2024-01-15")
                .active(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/books - Create Book")
    class CreateBookEndpointTests {

        @Test
        @DisplayName("Should create book successfully")
        void createBookSuccessfully() throws Exception {
            BookResponse response = createMockBookResponse(1L, "New Book", "New Author");
            when(bookService.createBook(ArgumentMatchers.any(BookCreateRequest.class))).thenReturn(response);

            BookCreateRequest request = BookCreateRequest.builder()
                    .title("New Book")
                    .author("New Author")
                    .isbn("1234567890123")
                    .price(new BigDecimal("29.99"))
                    .stock(100)
                    .build();

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("New Book"))
                    .andExpect(jsonPath("$.data.author").value("New Author"));
        }

        @Test
        @DisplayName("Should return 400 for validation errors")
        void createBookValidationError() throws Exception {
            BookCreateRequest request = BookCreateRequest.builder()
                    .title("")
                    .author("")
                    .build();

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 for duplicate ISBN (DuplicateBookException)")
        void createBookDuplicateIsbn() throws Exception {
            when(bookService.createBook(ArgumentMatchers.any(BookCreateRequest.class)))
                    .thenThrow(new DuplicateBookException("isbn", "123-456"));

            BookCreateRequest request = BookCreateRequest.builder()
                    .title("New Book")
                    .author("New Author")
                    .isbn("1234567890123")
                    .build();

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409));
        }
    }

    @Nested
    @DisplayName("GET /api/books/{id} - Get Book By ID")
    class GetBookByIdEndpointTests {

        @Test
        @DisplayName("Should get book by ID successfully")
        void getBookByIdSuccessfully() throws Exception {
            BookResponse response = createMockBookResponse(1L, "Test Book", "Test Author");
            when(bookService.getBookById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/books/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("Test Book"));
        }

        @Test
        @DisplayName("Should return 404 when book not found (BookNotFoundException)")
        void getBookByIdNotFound() throws Exception {
            when(bookService.getBookById(99L))
                    .thenThrow(new BookNotFoundException(99L));

            mockMvc.perform(get("/api/books/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }
    }

    @Nested
    @DisplayName("PUT /api/books/{id} - Update Book")
    class UpdateBookEndpointTests {

        @Test
        @DisplayName("Should update book successfully")
        void updateBookSuccessfully() throws Exception {
            BookResponse response = createMockBookResponse(1L, "Updated Book", "Test Author");
            when(bookService.updateBook(eq(1L), ArgumentMatchers.any(BookUpdateRequest.class))).thenReturn(response);

            BookUpdateRequest request = BookUpdateRequest.builder()
                    .title("Updated Book")
                    .build();

            mockMvc.perform(put("/api/books/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("Updated Book"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent book (BookNotFoundException)")
        void updateBookNotFound() throws Exception {
            when(bookService.updateBook(eq(99L), ArgumentMatchers.any(BookUpdateRequest.class)))
                    .thenThrow(new BookNotFoundException(99L));

            BookUpdateRequest request = BookUpdateRequest.builder()
                    .title("Updated")
                    .build();

            mockMvc.perform(put("/api/books/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/{id} - Delete Book")
    class DeleteBookEndpointTests {

        @Test
        @DisplayName("Should delete book successfully")
        void deleteBookSuccessfully() throws Exception {
            doNothing().when(bookService).deleteBook(1L);

            mockMvc.perform(delete("/api/books/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent book (BookNotFoundException)")
        void deleteBookNotFound() throws Exception {
            doThrow(new BookNotFoundException(99L)).when(bookService).deleteBook(99L);

            mockMvc.perform(delete("/api/books/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when deleting book with stock (InvalidBookStateException)")
        void deleteBookWithStock() throws Exception {
            doThrow(new InvalidBookStateException("Cannot delete book with remaining stock. Stock: 50"))
                    .when(bookService).deleteBook(1L);

            mockMvc.perform(delete("/api/books/1"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/books - Search Books")
    class SearchBooksEndpointTests {

        @Test
        @DisplayName("Should search books with default params")
        void searchBooksDefaultParams() throws Exception {
            PageResponse<BookResponse> page = PageResponse.of(
                    Collections.singletonList(createMockBookResponse(1L, "Test Book", "Author")),
                    1, 0, 10
            );
            when(bookService.searchBooks(ArgumentMatchers.any(BookSearchRequest.class))).thenReturn(page);

            mockMvc.perform(get("/api/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content", hasSize(1)));
        }

        @Test
        @DisplayName("Should search books with all filters")
        void searchBooksWithFilters() throws Exception {
            PageResponse<BookResponse> page = PageResponse.of(
                    Arrays.asList(
                            createMockBookResponse(1L, "Book 1", "Author 1"),
                            createMockBookResponse(2L, "Book 2", "Author 2")
                    ),
                    2, 0, 10
            );
            when(bookService.searchBooks(ArgumentMatchers.any(BookSearchRequest.class))).thenReturn(page);

            mockMvc.perform(get("/api/books")
                            .param("keyword", "test")
                            .param("author", "author")
                            .param("genre", "Fiction")
                            .param("minPrice", "10")
                            .param("maxPrice", "50")
                            .param("active", "true")
                            .param("sortBy", "price")
                            .param("sortDirection", "asc")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("GET /api/books/all - Get All Books")
    class GetAllBooksEndpointTests {

        @Test
        @DisplayName("Should get all books")
        void getAllBooks() throws Exception {
            List<BookResponse> books = Arrays.asList(
                    createMockBookResponse(1L, "Book 1", "Author 1"),
                    createMockBookResponse(2L, "Book 2", "Author 2")
            );
            when(bookService.getAllBooks()).thenReturn(books);

            mockMvc.perform(get("/api/books/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/books/{id}/stock - Update Stock")
    class UpdateStockEndpointTests {

        @Test
        @DisplayName("Should update stock successfully")
        void updateStockSuccessfully() throws Exception {
            BookResponse response = createMockBookResponse(1L, "Test Book", "Author");
            response.setStock(90);
            when(bookService.updateStock(1L, -10)).thenReturn(response);

            mockMvc.perform(patch("/api/books/1/stock")
                            .param("delta", "-10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stock").value(90));
        }

        @Test
        @DisplayName("Should return 400 for insufficient stock (InvalidBookStateException)")
        void updateStockInsufficient() throws Exception {
            when(bookService.updateStock(1L, -100))
                    .thenThrow(new InvalidBookStateException("Insufficient stock. Current: 5, Requested change: -100"));

            mockMvc.perform(patch("/api/books/1/stock")
                            .param("delta", "-100"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/books/{id}/toggle - Toggle Active")
    class ToggleActiveEndpointTests {

        @Test
        @DisplayName("Should toggle active status")
        void toggleActive() throws Exception {
            BookResponse response = createMockBookResponse(1L, "Test Book", "Author");
            response.setActive(false);
            when(bookService.toggleActive(1L)).thenReturn(response);

            mockMvc.perform(patch("/api/books/1/toggle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.active").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/books/export - Export Books")
    class ExportBooksEndpointTests {

        @Test
        @DisplayName("Should export as CSV")
        void exportCsv() throws Exception {
            ExportResult result = ExportResult.builder()
                    .fileName("books.csv")
                    .contentType("text/csv; charset=UTF-8")
                    .content("Title,Author\nBook 1,Author".getBytes())
                    .recordCount(1)
                    .build();

            when(bookService.exportBooks(ArgumentMatchers.any(BookSearchRequest.class), eq("csv"))).thenReturn(result);

            mockMvc.perform(get("/api/books/export")
                            .param("format", "csv"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should export as JSON")
        void exportJson() throws Exception {
            ExportResult result = ExportResult.builder()
                    .fileName("books.json")
                    .contentType("application/json; charset=UTF-8")
                    .content("[{}]".getBytes())
                    .recordCount(1)
                    .build();

            when(bookService.exportBooks(ArgumentMatchers.any(BookSearchRequest.class), eq("json"))).thenReturn(result);

            mockMvc.perform(get("/api/books/export")
                            .param("format", "json"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/books/ai-search - AI Smart Search")
    class AISmartSearchEndpointTests {

        @Test
        @DisplayName("Should perform AI smart search")
        void aiSmartSearch() throws Exception {
            List<BookResponse> results = Arrays.asList(
                    createMockBookResponse(1L, "Python Programming", "Author 1"),
                    createMockBookResponse(2L, "Python Basics", "Author 2")
            );
            when(bookService.aiSmartSearch("python")).thenReturn(results);

            mockMvc.perform(get("/api/books/ai-search")
                            .param("query", "python"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("Should return empty for no matches")
        void aiSmartSearchNoMatches() throws Exception {
            when(bookService.aiSmartSearch("xyznonexistent"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/books/ai-search")
                            .param("query", "xyznonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should handle BookNotFoundException for book not found")
        void handleBookNotFound() throws Exception {
            when(bookService.getBookById(99L))
                    .thenThrow(new BookNotFoundException(99L));

            mockMvc.perform(get("/api/books/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message", containsString("99")));
        }

        @Test
        @DisplayName("Should handle DuplicateBookException for duplicate ISBN")
        void handleDuplicateBook() throws Exception {
            when(bookService.createBook(ArgumentMatchers.any(BookCreateRequest.class)))
                    .thenThrow(new DuplicateBookException("isbn", "123"));

            BookCreateRequest request = BookCreateRequest.builder()
                    .title("Test")
                    .author("Test")
                    .isbn("1234567890123")
                    .build();

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409));
        }

        @Test
        @DisplayName("Should handle InvalidBookStateException for invalid book state")
        void handleInvalidBookState() throws Exception {
            when(bookService.updateStock(1L, -100))
                    .thenThrow(new InvalidBookStateException("Insufficient stock. Current: 5, Requested change: -100"));

            mockMvc.perform(patch("/api/books/1/stock")
                            .param("delta", "-100"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException")
        void handleIllegalArgument() throws Exception {
            when(bookService.exportBooks(ArgumentMatchers.any(BookSearchRequest.class), eq("invalid")))
                    .thenThrow(new IllegalArgumentException("Unsupported format"));

            mockMvc.perform(get("/api/books/export")
                            .param("format", "invalid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }
}
