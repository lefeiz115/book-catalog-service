package com.eyebuy.bookcatalog.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookEventListener {

    @EventListener
    public void onBookEvent(BookEvent event) {
        BookEvent.BookData data = event.getBookData();
        switch (event.getEventType()) {
            case CREATED -> log.info("Book created: id={}, title={}, author={}",
                    data.id(), data.title(), data.author());
            case UPDATED -> log.info("Book updated: id={}, title={}",
                    data.id(), data.title());
            case DELETED -> log.info("Book deleted: id={}, title={}",
                    data.id(), data.title());
            case STOCK_CHANGED -> log.info("Stock changed: id={}, title={}, stock={}, detail={}",
                    data.id(), data.title(), data.stock(), event.getDetail());
            case STATUS_CHANGED -> log.info("Status changed: id={}, title={}, active={}, detail={}",
                    data.id(), data.title(), data.active(), event.getDetail());
        }
    }
}
