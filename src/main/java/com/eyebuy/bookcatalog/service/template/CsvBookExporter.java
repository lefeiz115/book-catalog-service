package com.eyebuy.bookcatalog.service.template;

import com.eyebuy.bookcatalog.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;

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
                    .append(escapeCsvField(book.getIsbn())).append(",")
                    .append(escapeCsvField(book.getGenre())).append(",")
                    .append(book.getPrice() != null ? book.getPrice().toPlainString() : "").append(",")
                    .append(book.getStock() != null ? book.getStock() : 0).append(",")
                    .append(book.getPages() != null ? book.getPages() : 0).append(",")
                    .append(escapeCsvField(book.getPublisher())).append(",")
                    .append(book.getPublishDate() != null ? book.getPublishDate().toLocalDate().toString() : "").append(",")
                    .append(escapeCsvField(book.getDescription() != null ? book.getDescription().replace("\n", " ") : ""))
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    protected String getContentType() {
        return "text/csv; charset=UTF-8";
    }

    @Override
    protected String generateFileName() {
        return "books_export_" + System.currentTimeMillis() + ".csv";
    }
}
