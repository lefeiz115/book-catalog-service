package com.eyebuy.bookcatalog.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookEvent extends ApplicationEvent {

    private final BookData bookData;
    private final EventType eventType;
    private final String detail;

    public enum EventType {
        CREATED, UPDATED, DELETED, STOCK_CHANGED, STATUS_CHANGED
    }

    public record BookData(Long id, String title, String author, String genre, Integer stock, Boolean active) {
    }

    public BookEvent(Object source, BookData bookData, EventType eventType) {
        super(source);
        this.bookData = bookData;
        this.eventType = eventType;
        this.detail = null;
    }

    public BookEvent(Object source, BookData bookData, EventType eventType, String detail) {
        super(source);
        this.bookData = bookData;
        this.eventType = eventType;
        this.detail = detail;
    }
}
