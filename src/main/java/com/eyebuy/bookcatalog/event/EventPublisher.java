package com.eyebuy.bookcatalog.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishBookCreated(Object source, BookEvent.BookData bookData) {
        BookEvent event = new BookEvent(source, bookData, BookEvent.EventType.CREATED);
        applicationEventPublisher.publishEvent(event);
    }

    public void publishBookUpdated(Object source, BookEvent.BookData bookData) {
        BookEvent event = new BookEvent(source, bookData, BookEvent.EventType.UPDATED);
        applicationEventPublisher.publishEvent(event);
    }

    public void publishBookDeleted(Object source, BookEvent.BookData bookData) {
        BookEvent event = new BookEvent(source, bookData, BookEvent.EventType.DELETED);
        applicationEventPublisher.publishEvent(event);
    }

    public void publishStockChanged(Object source, BookEvent.BookData bookData, String detail) {
        BookEvent event = new BookEvent(source, bookData, BookEvent.EventType.STOCK_CHANGED, detail);
        applicationEventPublisher.publishEvent(event);
    }

    public void publishStatusChanged(Object source, BookEvent.BookData bookData, String detail) {
        BookEvent event = new BookEvent(source, bookData, BookEvent.EventType.STATUS_CHANGED, detail);
        applicationEventPublisher.publishEvent(event);
    }
}
