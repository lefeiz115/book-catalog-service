package com.eyebuy.bookcatalog.dto;

import com.eyebuy.bookcatalog.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private String genreDisplayName;
    private BigDecimal price;
    private String description;
    private Integer stock;
    private Integer pages;
    private String publisher;
    private String publishDate;
    private Boolean active;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static BookResponse fromEntity(Book book) {
        if (book == null) {
            return null;
        }
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .genre(book.getGenre())
                .price(book.getPrice())
                .description(book.getDescription())
                .stock(book.getStock())
                .pages(book.getPages())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate() != null ? book.getPublishDate().toString() : null)
                .active(book.getActive())
                .createTime(book.getCreateTime())
                .updateTime(book.getUpdateTime())
                .build();
    }
}
