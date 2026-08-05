package com.eyebuy.bookcatalog.dto;

import com.eyebuy.bookcatalog.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO Tests")
class DtoTest {

    @Test
    @DisplayName("BookResponse.fromEntity should map correctly")
    void bookResponseFromEntity() {
        Book book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .isbn("1234567890123")
                .genre("Fiction")
                .price(new BigDecimal("29.99"))
                .description("Test description")
                .stock(100)
                .pages(300)
                .publisher("Test Publisher")
                .publishDate(LocalDateTime.of(2024, 1, 15, 0, 0))
                .active(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        BookResponse response = BookResponse.fromEntity(book);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Book");
        assertThat(response.getAuthor()).isEqualTo("Test Author");
        assertThat(response.getPublishDate()).isEqualTo("2024-01-15T00:00");
    }

    @Test
    @DisplayName("BookResponse.fromEntity should return null for null input")
    void bookResponseFromEntityNull() {
        assertThat(BookResponse.fromEntity(null)).isNull();
    }

    @Test
    @DisplayName("PageResponse.of should create correct pagination")
    void pageResponseOf() {
        List<String> content = List.of("A", "B", "C");
        PageResponse<String> response = PageResponse.of(content, 25, 0, 10);

        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getTotalElements()).isEqualTo(25);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();
    }

    @Test
    @DisplayName("PageResponse.of should handle empty content")
    void pageResponseOfEmpty() {
        PageResponse<String> response = PageResponse.of(Collections.emptyList(), 0, 0, 10);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
    }

    @Test
    @DisplayName("ApiResponse.success should create success response")
    void apiResponseSuccess() {
        ApiResponse<String> response = ApiResponse.success("test");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("test");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("ApiResponse.error with custom message should create response")
    void apiResponseErrorWithMessage() {
        ApiResponse<Void> response = ApiResponse.error(500, "Server error");

        assertThat(response.getCode()).isEqualTo(500);
        assertThat(response.getMessage()).isEqualTo("Server error");
    }

    @Test
    @DisplayName("ApiResponse.error should create error response")
    void apiResponseError() {
        ApiResponse<Void> response = ApiResponse.error(404, "Not found");

        assertThat(response.getCode()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Not found");
    }

    @Test
    @DisplayName("ExportResult should be built correctly")
    void exportResultBuild() {
        ExportResult result = ExportResult.builder()
                .fileName("test.csv")
                .contentType("text/csv")
                .content("data".getBytes())
                .recordCount(10)
                .build();

        assertThat(result.getFileName()).isEqualTo("test.csv");
        assertThat(result.getContentType()).isEqualTo("text/csv");
        assertThat(result.getContent()).isEqualTo("data".getBytes());
        assertThat(result.getRecordCount()).isEqualTo(10);
    }
}
