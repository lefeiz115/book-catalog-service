package com.eyebuy.bookcatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchRequest {
    private String keyword;
    private String author;
    private String genre;
    private String isbn;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean active;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}
