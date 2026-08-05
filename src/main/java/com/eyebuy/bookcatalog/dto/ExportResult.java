package com.eyebuy.bookcatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResult {
    private String fileName;
    private String contentType;
    private byte[] content;
    private int recordCount;
}
