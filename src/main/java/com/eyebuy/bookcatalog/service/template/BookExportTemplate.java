package com.eyebuy.bookcatalog.service.template;

import com.eyebuy.bookcatalog.dto.ExportResult;
import com.eyebuy.bookcatalog.entity.Book;

import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class BookExportTemplate {

    public final ExportResult export(List<Book> books) {
        validateData(books);
        String fileName = generateFileName();
        String contentType = getContentType();
        String header = buildHeader();
        String body = buildBody(books);
        String footer = buildFooter();
        byte[] content = (header + body + footer).getBytes(StandardCharsets.UTF_8);
        return ExportResult.builder()
                .fileName(fileName)
                .contentType(contentType)
                .content(content)
                .recordCount(books.size())
                .build();
    }

    protected void validateData(List<Book> books) {
        if (books == null) {
            throw new IllegalArgumentException("Books list cannot be null");
        }
    }

    protected abstract String buildHeader();
    protected abstract String buildBody(List<Book> books);

    protected String buildFooter() {
        return "";
    }

    protected abstract String getContentType();
    protected abstract String generateFileName();

    protected String escapeCsvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
