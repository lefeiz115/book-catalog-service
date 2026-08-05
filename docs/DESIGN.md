# Design Patterns

> Book Catalog Service — Design Pattern Implementation Details

---

## Overview

This project implements **3 design patterns** to demonstrate clean architecture and SOLID principles:

| # | Pattern | Purpose | Location |
|---|---------|---------|----------|
| 1 | **Strategy** | Multi-dimensional search | `service/strategy/` |
| 2 | **Template Method** | Data export (CSV/JSON) | `service/template/` |
| 3 | **Observer** | Event-driven audit logging | `event/` |

---

## 1. Strategy Pattern — Multi-dimensional Search

### Problem

Books can be searched by different dimensions: title, author, genre. Without the Strategy pattern, this would require either:
- A giant `if-else` switch in the service method
- Multiple duplicated methods

### Solution

Define a common interface, implement each search dimension as a separate strategy, and inject them via Spring's `@Component` + `Map<String, Strategy>` auto-wiring.

### Structure

```
┌─────────────────────┐
│  <<interface>>       │
│  BookSearchStrategy  │
├─────────────────────┤
│ + getName(): String  │
│ + execute(value): List│
└─────────┬───────────┘
          │
    ┌─────┼─────────────┐
    │     │             │
┌───▼───┐ ┌───▼───┐ ┌───▼───┐
│Title  │ │Author │ │Genre  │
│Strategy│ │Strategy│ │Strategy│
└───────┘ └───────┘ └───────┘
```

### Code

```java
// Strategy interface
public interface BookSearchStrategy {
    String getName();
    List<Book> execute(String value);
}

// Concrete strategies
@Component("title")
public class TitleSearchStrategy implements BookSearchStrategy {
    private final BookRepository bookRepository;

    @Override
    public List<Book> execute(String value) {
        return bookRepository.findByTitleContainingIgnoreCase(value);
    }
}

@Component("author")
public class AuthorSearchStrategy implements BookSearchStrategy { ... }

@Component("genre")
public class GenreSearchStrategy implements BookSearchStrategy { ... }
```

### Benefits

- **Open/Closed Principle**: Add new search dimensions (e.g., `PublisherSearchStrategy`) without modifying existing code
- **Single Responsibility**: Each strategy handles one search dimension
- **Testability**: Each strategy can be unit-tested in isolation

---

## 2. Template Method Pattern — Data Export

### Problem

CSV and JSON exports share the same algorithm structure (validate → header → body → footer → assemble), but differ in format-specific details.

### Solution

Define the algorithm skeleton in an abstract base class, let subclasses override only the format-specific steps.

### Structure

```
┌──────────────────────────┐
│  <<abstract>>             │
│  BookExportTemplate       │
├──────────────────────────┤
│ + export(books): ExportResult  │  ← Template method (final)
│ # validateData(books)          │  ← Fixed step
│ # buildHeader(): String        │  ← Abstract step
│ # buildBody(books): String     │  ← Abstract step
│ # buildFooter(): String        │  ← Hook (optional override)
│ # escapeCsvField(value)        │  ← Utility method
└────────────┬─────────────┘
             │
     ┌───────┴────────┐
     │                │
┌────▼────┐    ┌──────▼────┐
│CsvBook  │    │JsonBook   │
│Exporter │    │Exporter   │
└─────────┘    └───────────┘
```

### Code

```java
public abstract class BookExportTemplate {

    // Template method — defines the algorithm skeleton
    public ExportResult export(List<Book> books) {
        validateData(books);
        String fileName = generateFileName();
        String contentType = getContentType();
        String header = buildHeader();        // Abstract — subclass implements
        String body = buildBody(books);       // Abstract — subclass implements
        String footer = buildFooter();        // Hook — default empty, subclass can override
        byte[] content = (header + body + footer).getBytes(StandardCharsets.UTF_8);
        return ExportResult.builder()
                .fileName(fileName)
                .contentType(contentType)
                .content(content)
                .recordCount(books.size())
                .build();
    }

    protected abstract String buildHeader();
    protected abstract String buildBody(List<Book> books);

    // Hook method — default implementation, subclasses MAY override
    protected String buildFooter() {
        return "";
    }
}

@Component("csv")
public class CsvBookExporter extends BookExportTemplate {
    @Override
    protected String buildHeader() {
        return "Title,Author,ISBN,Genre,Price,Stock,Pages,Publisher,PublishDate,Description\n";
    }

    @Override
    protected String buildBody(List<Book> books) {
        StringBuilder sb = new StringBuilder();
        for (Book book : books) {
            sb.append(escapeCsvField(book.getTitle())).append(",")
              .append(escapeCsvField(book.getAuthor())).append(",")
              // ... more fields
              .append("\n");
        }
        return sb.toString();
    }
}

@Component("json")
public class JsonBookExporter extends BookExportTemplate {
    @Override
    protected String buildHeader() { return "["; }

    @Override
    protected String buildBody(List<Book> books) {
        // Serialize each book to JSON
    }

    @Override
    protected String buildFooter() { return "]"; }  // Override hook
}
```

### Benefits

- **DRY (Don't Repeat Yourself)**: Shared logic lives in the template
- **Extensibility**: Add `XmlBookExporter` or `PdfBookExporter` by extending the template
- **Consistency**: All exporters follow the same algorithm structure

---

## 3. Observer Pattern — Event-driven Audit

### Problem

When a book is created/updated/deleted, we need to log audit trails, potentially notify external systems, or trigger downstream processing — without coupling these concerns to the business logic.

### Solution

Use Spring's `ApplicationEventPublisher` and `@EventListener` to implement the Observer pattern. The service publishes events; listeners react independently.

### Structure

```
┌─────────────────┐         ┌──────────────────┐
│  EventPublisher  │────────▶│  ApplicationEvent │
│  (Subject)       │ publish │  Publisher (Spring)│
└─────────────────┘         └────────┬─────────┘
                                     │ notify
                            ┌────────▼─────────┐
                            │  BookEventListener │
                            │  (Observer)        │
                            └──────────────────┘
```

### Code

```java
// Event definition
public class BookEvent extends ApplicationEvent {
    private final BookData bookData;
    private final EventType eventType;  // CREATED, UPDATED, DELETED, STOCK_CHANGED, STATUS_CHANGED
    private final String detail;

    public record BookData(Long id, String title, String author,
                           String genre, Integer stock, Boolean active) {}
}

// Publisher (Subject)
@Component
public class EventPublisher {
    private final ApplicationEventPublisher publisher;

    public void publishBookCreated(Object source, BookData data) {
        publisher.publishEvent(new BookEvent(source, data, EventType.CREATED));
    }

    public void publishStockChanged(Object source, BookData data, String detail) {
        publisher.publishEvent(new BookEvent(source, data, EventType.STOCK_CHANGED, detail));
    }
}

// Listener (Observer)
@Component
public class BookEventListener {
    @EventListener
    public void onBookEvent(BookEvent event) {
        switch (event.getEventType()) {
            case CREATED -> log.info("📚 Book created: id={}, title={}",
                    event.getBookData().id(), event.getBookData().title());
            case STOCK_CHANGED -> log.info("📦 Stock changed: id={}, detail={}",
                    event.getBookData().id(), event.getDetail());
            // ... other cases
        }
    }
}

// Usage in service
eventPublisher.publishBookCreated(this, toBookData(savedBook));
```

### Benefits

- **Decoupling**: Service logic doesn't know about audit/logging concerns
- **Extensibility**: Add new listeners (e.g., `NotificationListener`, `MetricsListener`) without touching the service
- **Spring Integration**: Leverages Spring's built-in event mechanism — no external message broker needed

---

## Pattern Synergy

```
┌─────────────────────────────────────────────────┐
│              BookServiceImpl                     │
├─────────────────────────────────────────────────┤
│                                                  │
│  Search ──▶ Strategy Pattern (pluggable search)  │
│                                                  │
│  Export ──▶ Template Method (shared algorithm)   │
│                                                  │
│  CRUD ────▶ Observer Pattern (audit events)      │
│                                                  │
└─────────────────────────────────────────────────┘
```

The three patterns work together:
1. **Strategy** makes search extensible
2. **Template Method** makes export maintainable
3. **Observer** makes the system event-driven and auditable
