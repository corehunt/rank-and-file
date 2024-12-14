package com.rankandfile.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TextDTO {
    private String textId;
    private LocalDate versionDate;
    private String versionType;
    private String pdfUrl;
}
