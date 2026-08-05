package com.eyebuy.bookcatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {

    @NotBlank(message = "书名不能为空")
    @Size(max = 500, message = "书名长度不能超过500字符")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 200, message = "作者长度不能超过200字符")
    private String author;

    @Size(max = 20, message = "ISBN长度不能超过20字符")
    private String isbn;

    @Size(max = 50, message = "类型长度不能超过50字符")
    private String genre;

    private BigDecimal price;

    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;

    private Integer stock;

    private Integer pages;

    @Size(max = 50, message = "出版社长度不能超过50字符")
    private String publisher;

    private String publishDate;
}
