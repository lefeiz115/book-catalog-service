package com.eyebuy.bookcatalog.service.template;

import com.eyebuy.bookcatalog.dto.ExportResult;
import com.eyebuy.bookcatalog.entity.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Export Template Tests")
class BookExportTemplateTest {

    private Book createTestBook(String title, String author, String isbn) {
        return Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .genre("Fiction")
                .price(new BigDecimal("29.99"))
                .stock(100)
                .pages(300)
                .publisher("Test Publisher")
                .build();
    }

    @Test
    @DisplayName("CSV exporter should export books correctly")
    void csvExport() {
        CsvBookExporter exporter = new CsvBookExporter();

        ExportResult result = exporter.export(Arrays.asList(
                createTestBook("Book 1", "Author 1", "1111111111111"),
                createTestBook("Book 2", "Author 2", "2222222222222")
        ));

        assertThat(result.getFileName()).endsWith(".csv");
        assertThat(result.getContentType()).isEqualTo("text/csv; charset=UTF-8");
        assertThat(result.getRecordCount()).isEqualTo(2);
        assertThat(result.getContent()).isNotNull();

        String content = new String(result.getContent());
        assertThat(content).contains("Title,Author,ISBN");
        assertThat(content).contains("Book 1");
        assertThat(content).contains("Author 1");
        assertThat(content).contains("Book 2");
    }

    @Test
    @DisplayName("CSV exporter should handle empty list")
    void csvExportEmpty() {
        CsvBookExporter exporter = new CsvBookExporter();
        ExportResult result = exporter.export(Collections.emptyList());

        assertThat(result.getRecordCount()).isEqualTo(0);
        String content = new String(result.getContent());
        assertThat(content).contains("Title,Author");
    }

    @Test
    @DisplayName("CSV exporter should escape special characters")
    void csvExportEscape() {
        CsvBookExporter exporter = new CsvBookExporter();
        Book book = createTestBook("Book, With Comma", "Author", "1111111111111");
        ExportResult result = exporter.export(Collections.singletonList(book));

        String content = new String(result.getContent());
        assertThat(content).contains("\"Book, With Comma\"");
    }

    @Test
    @DisplayName("JSON exporter should export books correctly")
    void jsonExport() {
        JsonBookExporter exporter = new JsonBookExporter(new ObjectMapper());

        ExportResult result = exporter.export(Arrays.asList(
                createTestBook("Book 1", "Author 1", "1111111111111"),
                createTestBook("Book 2", "Author 2", "2222222222222")
        ));

        assertThat(result.getFileName()).endsWith(".json");
        assertThat(result.getContentType()).isEqualTo("application/json; charset=UTF-8");
        assertThat(result.getRecordCount()).isEqualTo(2);

        String content = new String(result.getContent());
        assertThat(content).startsWith("[");
        assertThat(content).endsWith("]");
        assertThat(content).contains("Book 1");
    }

    @Test
    @DisplayName("JSON exporter should handle empty list")
    void jsonExportEmpty() {
        JsonBookExporter exporter = new JsonBookExporter(new ObjectMapper());
        ExportResult result = exporter.export(Collections.emptyList());

        assertThat(result.getRecordCount()).isEqualTo(0);
        String content = new String(result.getContent());
        assertThat(content).isEqualTo("[]");
    }

    @Test
    @DisplayName("CSV exporter should throw on null list")
    void csvExportNull() {
        CsvBookExporter exporter = new CsvBookExporter();
        assertThatThrownBy(() -> exporter.export(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
