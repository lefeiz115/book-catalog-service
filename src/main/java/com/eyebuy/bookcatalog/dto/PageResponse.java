package com.eyebuy.bookcatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> of(List<T> content, long totalElements, int number, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        return PageResponse.<T>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .number(number)
                .size(size)
                .first(number == 0)
                .last(number >= totalPages - 1)
                .build();
    }
}
