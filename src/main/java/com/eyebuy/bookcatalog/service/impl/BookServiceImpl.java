package com.eyebuy.bookcatalog.service.impl;

import com.eyebuy.bookcatalog.dto.*;
import com.eyebuy.bookcatalog.entity.Book;
import com.eyebuy.bookcatalog.event.BookEvent;
import com.eyebuy.bookcatalog.event.EventPublisher;
import com.eyebuy.bookcatalog.exception.BookNotFoundException;
import com.eyebuy.bookcatalog.exception.DuplicateBookException;
import com.eyebuy.bookcatalog.exception.InvalidBookStateException;
import com.eyebuy.bookcatalog.repository.BookRepository;
import com.eyebuy.bookcatalog.service.BookService;
import com.eyebuy.bookcatalog.service.template.BookExportTemplate;
import com.eyebuy.bookcatalog.service.template.CsvBookExporter;
import com.eyebuy.bookcatalog.service.template.JsonBookExporter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "books")
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final EventPublisher eventPublisher;
    private final CsvBookExporter csvExporter;
    private final JsonBookExporter jsonExporter;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional
    public BookResponse createBook(BookCreateRequest request) {
        if (StringUtils.hasText(request.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateBookException("isbn", request.getIsbn());
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .genre(request.getGenre())
                .price(request.getPrice())
                .description(request.getDescription())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .pages(request.getPages() != null ? request.getPages() : 0)
                .publisher(request.getPublisher())
                .publishDate(parseDate(request.getPublishDate()))
                .active(true)
                .build();

        Book saved = bookRepository.save(book);

        eventPublisher.publishBookCreated(this, toBookData(saved));

        log.info("Book created successfully: id={}, title={}", saved.getId(), saved.getTitle());
        return BookResponse.fromEntity(saved);
    }

    @Override
    @Cacheable(key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return BookResponse.fromEntity(book);
    }

    @Override
    @Transactional
    @CacheEvict(key = "#id")
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (StringUtils.hasText(request.getIsbn())
                && !request.getIsbn().equals(book.getIsbn())) {
            bookRepository.findByIsbnAndIdNot(request.getIsbn(), id)
                    .ifPresent(existing -> {
                        throw new DuplicateBookException("isbn", request.getIsbn());
                    });
        }

        updateBookFields(book, request);
        Book updated = bookRepository.save(book);

        eventPublisher.publishBookUpdated(this, toBookData(updated));

        log.info("Book updated successfully: id={}, title={}", updated.getId(), updated.getTitle());
        return BookResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    @CacheEvict(key = "#id")
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (book.getStock() != null && book.getStock() > 0) {
            throw new InvalidBookStateException("Cannot delete book with remaining stock. Stock: " + book.getStock());
        }

        bookRepository.delete(book);

        eventPublisher.publishBookDeleted(this, toBookData(book));

        log.info("Book deleted successfully: id={}, title={}", book.getId(), book.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> searchBooks(BookSearchRequest request) {
        Specification<Book> spec = buildSpecification(request);

        Sort sort = buildSort(request);
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                sort
        );

        Page<Book> page = bookRepository.findAll(spec, pageable);

        List<BookResponse> content = page.getContent().stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.of(content, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    @Transactional
    @CacheEvict(key = "#id")
    public BookResponse updateStock(Long id, Integer stockDelta) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        int currentStock = book.getStock() != null ? book.getStock() : 0;
        int newStock = currentStock + stockDelta;

        if (newStock < 0) {
            throw new InvalidBookStateException(
                    String.format("Insufficient stock. Current: %d, Requested change: %d", currentStock, stockDelta));
        }

        book.setStock(newStock);
        Book updated = bookRepository.save(book);

        String detail = String.format("Stock changed from %d to %d (delta: %d)", currentStock, newStock, stockDelta);
        eventPublisher.publishStockChanged(this, toBookData(updated), detail);

        log.info("Stock updated: id={}, {} -> {}", id, currentStock, newStock);
        return BookResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    @CacheEvict(key = "#id")
    public BookResponse toggleActive(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        boolean newStatus = !Boolean.TRUE.equals(book.getActive());
        book.setActive(newStatus);
        Book updated = bookRepository.save(book);

        String detail = String.format("Status changed to: %s", newStatus ? "active" : "inactive");
        eventPublisher.publishStatusChanged(this, toBookData(updated), detail);

        log.info("Book status toggled: id={}, active={}", id, newStatus);
        return BookResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ExportResult exportBooks(BookSearchRequest request, String format) {
        Specification<Book> spec = buildSpecification(request);
        List<Book> books = bookRepository.findAll(spec);

        BookExportTemplate exporter = getExporter(format);
        return exporter.export(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> aiSmartSearch(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        String normalizedQuery = query.trim().toLowerCase();

        List<Book> allBooks = bookRepository.findByActiveTrue();

        return allBooks.stream()
                .filter(book -> matchesRelevance(book, normalizedQuery))
                .sorted((b1, b2) -> calculateRelevanceScore(b2, normalizedQuery)
                        - calculateRelevanceScore(b1, normalizedQuery))
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private boolean matchesRelevance(Book book, String query) {
        if (StringUtils.hasText(book.getTitle()) && book.getTitle().toLowerCase().contains(query)) {
            return true;
        }
        if (StringUtils.hasText(book.getAuthor()) && book.getAuthor().toLowerCase().contains(query)) {
            return true;
        }
        if (StringUtils.hasText(book.getDescription()) && book.getDescription().toLowerCase().contains(query)) {
            return true;
        }
        if (StringUtils.hasText(book.getGenre()) && book.getGenre().toLowerCase().contains(query)) {
            return true;
        }

        String[] tokens = query.split("\\s+");
        for (String token : tokens) {
            if (token.length() < 2) continue;
            if (StringUtils.hasText(book.getTitle()) && book.getTitle().toLowerCase().contains(token)) {
                return true;
            }
            if (StringUtils.hasText(book.getAuthor()) && book.getAuthor().toLowerCase().contains(token)) {
                return true;
            }
            if (StringUtils.hasText(book.getDescription()) && book.getDescription().toLowerCase().contains(token)) {
                return true;
            }
        }
        return false;
    }

    private int calculateRelevanceScore(Book book, String query) {
        int score = 0;
        String title = book.getTitle() != null ? book.getTitle().toLowerCase() : "";
        String author = book.getAuthor() != null ? book.getAuthor().toLowerCase() : "";
        String description = book.getDescription() != null ? book.getDescription().toLowerCase() : "";

        if (title.equals(query)) score += 100;
        else if (title.startsWith(query)) score += 80;
        else if (title.contains(query)) score += 50;

        if (author.equals(query)) score += 70;
        else if (author.startsWith(query)) score += 50;
        else if (author.contains(query)) score += 30;

        if (description.contains(query)) score += 10;

        String[] tokens = query.split("\\s+");
        for (String token : tokens) {
            if (token.length() < 2) continue;
            if (title.contains(token)) score += 20;
            if (author.contains(token)) score += 15;
            if (description.contains(token)) score += 5;
        }

        return score;
    }

    private BookExportTemplate getExporter(String format) {
        if (format == null || format.isBlank() || "csv".equalsIgnoreCase(format)) {
            return csvExporter;
        }
        if ("json".equalsIgnoreCase(format)) {
            return jsonExporter;
        }
        throw new IllegalArgumentException("Unsupported export format: " + format + ". Supported: csv, json");
    }

    private Specification<Book> buildSpecification(BookSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getKeyword())) {
                String likePattern = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likePattern),
                        cb.like(cb.lower(root.get("author")), likePattern),
                        cb.like(cb.lower(root.get("description")), likePattern)
                ));
            }

            if (StringUtils.hasText(request.getAuthor())) {
                predicates.add(cb.like(cb.lower(root.get("author")),
                        "%" + request.getAuthor().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(request.getGenre())) {
                predicates.add(cb.equal(root.get("genre"), request.getGenre()));
            }

            if (StringUtils.hasText(request.getIsbn())) {
                predicates.add(cb.like(cb.lower(root.get("isbn")),
                        "%" + request.getIsbn().toLowerCase() + "%"));
            }

            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }

            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            if (request.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), request.getActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(BookSearchRequest request) {
        if (!StringUtils.hasText(request.getSortBy())) {
            return Sort.by(Sort.Direction.DESC, "updateTime");
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (request.getSortBy().toLowerCase()) {
            case "title" -> Sort.by(direction, "title");
            case "author" -> Sort.by(direction, "author");
            case "price" -> Sort.by(direction, "price");
            case "stock" -> Sort.by(direction, "stock");
            case "createtime", "create_time" -> Sort.by(direction, "createTime");
            case "updatetime", "update_time" -> Sort.by(direction, "updateTime");
            default -> Sort.by(Sort.Direction.DESC, "updateTime");
        };
    }

    private void updateBookFields(Book book, BookUpdateRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            book.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getAuthor())) {
            book.setAuthor(request.getAuthor());
        }
        if (request.getIsbn() != null) {
            book.setIsbn(request.getIsbn());
        }
        if (request.getGenre() != null) {
            book.setGenre(request.getGenre());
        }
        if (request.getPrice() != null) {
            book.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        if (request.getStock() != null) {
            book.setStock(request.getStock());
        }
        if (request.getPages() != null) {
            book.setPages(request.getPages());
        }
        if (request.getPublisher() != null) {
            book.setPublisher(request.getPublisher());
        }
        if (request.getPublishDate() != null) {
            book.setPublishDate(parseDate(request.getPublishDate()));
        }
        if (request.getActive() != null) {
            book.setActive(request.getActive());
        }
    }

    private LocalDateTime parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER).atStartOfDay();
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private BookEvent.BookData toBookData(Book book) {
        return new BookEvent.BookData(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getStock(),
                book.getActive()
        );
    }
}
