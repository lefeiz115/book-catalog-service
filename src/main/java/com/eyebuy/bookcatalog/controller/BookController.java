package com.eyebuy.bookcatalog.controller;

import com.eyebuy.bookcatalog.dto.ApiResponse;
import com.eyebuy.bookcatalog.dto.BookCreateRequest;
import com.eyebuy.bookcatalog.dto.BookResponse;
import com.eyebuy.bookcatalog.dto.BookSearchRequest;
import com.eyebuy.bookcatalog.dto.BookUpdateRequest;
import com.eyebuy.bookcatalog.dto.ExportResult;
import com.eyebuy.bookcatalog.dto.PageResponse;
import com.eyebuy.bookcatalog.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Catalog", description = "Book management APIs")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Create a new book", description = "Add a new book to the catalog")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Book created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate ISBN")
    })
    public ApiResponse<BookResponse> createBook(
            @Valid @RequestBody BookCreateRequest request) {
        return ApiResponse.success(bookService.createBook(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID", description = "Retrieve a book's details by its ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Book found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ApiResponse<BookResponse> getBookById(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        return ApiResponse.success(bookService.getBookById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Update book information")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate ISBN")
    })
    public ApiResponse<BookResponse> updateBook(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        return ApiResponse.success(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Delete a book from the catalog")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Book deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete book with remaining stock")
    })
    public ApiResponse<Void> deleteBook(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success(null);
    }

    @GetMapping
    @Operation(summary = "Search books", description = "Search and filter books with pagination")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned")
    })
    public ApiResponse<PageResponse<BookResponse>> searchBooks(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Author name") @RequestParam(required = false) String author,
            @Parameter(description = "Book genre") @RequestParam(required = false) String genre,
            @Parameter(description = "ISBN") @RequestParam(required = false) String isbn,
            @Parameter(description = "Minimum price") @RequestParam(required = false) java.math.BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "updateTime") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size) {

        BookSearchRequest request = BookSearchRequest.builder()
                .keyword(keyword)
                .author(author)
                .genre(genre)
                .isbn(isbn)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .active(active)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        return ApiResponse.success(bookService.searchBooks(request));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all books", description = "Retrieve all books without pagination")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All books returned")
    })
    public ApiResponse<List<BookResponse>> getAllBooks() {
        return ApiResponse.success(bookService.getAllBooks());
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update book stock", description = "Adjust book stock quantity")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient stock")
    })
    public ApiResponse<BookResponse> updateStock(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Parameter(description = "Stock delta (positive to add, negative to remove)")
            @RequestParam Integer delta) {
        return ApiResponse.success(bookService.updateStock(id, delta));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle book active status", description = "Activate or deactivate a book")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status toggled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ApiResponse<BookResponse> toggleActive(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        return ApiResponse.success(bookService.toggleActive(id));
    }

    @GetMapping("/export")
    @Operation(summary = "Export books", description = "Export books to CSV or JSON format")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Export successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Unsupported format")
    })
    public ResponseEntity<byte[]> exportBooks(
            @Parameter(description = "Export format: csv or json") @RequestParam(defaultValue = "csv") String format,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Author name") @RequestParam(required = false) String author,
            @Parameter(description = "Book genre") @RequestParam(required = false) String genre) {

        BookSearchRequest request = BookSearchRequest.builder()
                .keyword(keyword)
                .author(author)
                .genre(genre)
                .build();

        ExportResult result = bookService.exportBooks(request, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.getContentType()));
        headers.setContentDispositionFormData("attachment", result.getFileName());
        headers.setContentLength(result.getContent().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.getContent());
    }

    @GetMapping("/ai-search")
    @Operation(summary = "AI smart search", description = "AI-powered intelligent book search with relevance ranking")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI search results returned")
    })
    public ApiResponse<List<BookResponse>> aiSmartSearch(
            @Parameter(description = "Natural language query") @RequestParam String query) {
        return ApiResponse.success(bookService.aiSmartSearch(query));
    }
}
