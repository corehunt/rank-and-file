package com.rankandfile.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RelatedBillDTO {
    private String billId;
    private Integer billNo;
    private String billTitle;
    private LocalDate introducedDt;
    private Integer congress;
    private String billType;
    private String originChamber;
}
