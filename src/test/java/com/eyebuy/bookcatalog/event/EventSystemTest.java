package com.eyebuy.bookcatalog.event;

import com.eyebuy.bookcatalog.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Event System Tests")
class EventSystemTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private EventPublisher eventPublisher;

    @InjectMocks
    private BookEventListener listener;

    private Book createTestBook(Long id, String title, String author, boolean active) {
        return Book.builder()
                .id(id)
                .title(title)
                .author(author)
                .isbn("1234567890123")
                .genre("Fiction")
                .price(new BigDecimal("29.99"))
                .stock(active ? 100 : 0)
                .active(active)
                .build();
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

    @Test
    @DisplayName("Should publish book created event")
    void publishBookCreated() {
        Book book = createTestBook(1L, "Test Book", "Author", true);
        BookEvent.BookData bookData = toBookData(book);

        eventPublisher.publishBookCreated(eventPublisher, bookData);

        verify(applicationEventPublisher).publishEvent(any(BookEvent.class));
    }

    @Test
    @DisplayName("Should publish book updated event")
    void publishBookUpdated() {
        Book book = createTestBook(1L, "Updated Book", "Author", true);
        BookEvent.BookData bookData = toBookData(book);

        eventPublisher.publishBookUpdated(eventPublisher, bookData);

        verify(applicationEventPublisher).publishEvent(any(BookEvent.class));
    }

    @Test
    @DisplayName("Should publish book deleted event")
    void publishBookDeleted() {
        Book book = createTestBook(1L, "Deleted Book", "Author", false);
        BookEvent.BookData bookData = toBookData(book);

        eventPublisher.publishBookDeleted(eventPublisher, bookData);

        verify(applicationEventPublisher).publishEvent(any(BookEvent.class));
    }

    @Test
    @DisplayName("Should publish stock changed event")
    void publishStockChanged() {
        Book book = createTestBook(1L, "Book", "Author", true);
        BookEvent.BookData bookData = toBookData(book);

        eventPublisher.publishStockChanged(eventPublisher, bookData, "Stock changed from 100 to 90");

        verify(applicationEventPublisher).publishEvent(any(BookEvent.class));
    }

    @Test
    @DisplayName("Should publish status changed event")
    void publishStatusChanged() {
        Book book = createTestBook(1L, "Book", "Author", false);
        BookEvent.BookData bookData = toBookData(book);

        eventPublisher.publishStatusChanged(eventPublisher, bookData, "Status changed to: inactive");

        verify(applicationEventPublisher).publishEvent(any(BookEvent.class));
    }

    @Test
    @DisplayName("BookEvent should hold correct data for created event")
    void bookEventData() {
        Book book = createTestBook(1L, "Test", "Author", true);
        BookEvent.BookData bookData = toBookData(book);
        BookEvent event = new BookEvent(eventPublisher, bookData, BookEvent.EventType.CREATED);

        assertThat(event.getEventType()).isEqualTo(BookEvent.EventType.CREATED);
        assertThat(event.getBookData().id()).isEqualTo(1L);
        assertThat(event.getBookData().title()).isEqualTo("Test");
        assertThat(event.getBookData().author()).isEqualTo("Author");
        assertThat(event.getDetail()).isNull();
    }

    @Test
    @DisplayName("BookEvent with detail should hold detail message")
    void bookEventWithDetail() {
        Book book = createTestBook(1L, "Test", "Author", true);
        BookEvent.BookData bookData = toBookData(book);
        BookEvent event = new BookEvent(eventPublisher, bookData, BookEvent.EventType.STOCK_CHANGED,
                "Stock changed from 50 to 100");

        assertThat(event.getDetail()).contains("Stock changed");
    }
}
