package com.eyebuy.bookcatalog.service;

import com.eyebuy.bookcatalog.dto.*;

import java.util.List;

public interface BookService {

    BookResponse createBook(BookCreateRequest request);

    BookResponse getBookById(Long id);

    BookResponse updateBook(Long id, BookUpdateRequest request);

    void deleteBook(Long id);

    List<BookResponse> getAllBooks();

    PageResponse<BookResponse> searchBooks(BookSearchRequest request);

    BookResponse updateStock(Long id, Integer stockDelta);

    BookResponse toggleActive(Long id);

    ExportResult exportBooks(BookSearchRequest request, String format);

    List<BookResponse> aiSmartSearch(String query);
}
