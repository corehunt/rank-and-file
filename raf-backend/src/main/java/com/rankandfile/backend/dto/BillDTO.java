package com.rankandfile.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BillDTO {
    private String billId;
    private Integer billNo;
    private String billTitle;
    private LocalDate introducedDt;
    private LocalDate latestActionDt;
    private String latestActionTxt;
    private String policyArea;
    private Integer congress;
    private String billType;
    private String originChamber;
    private String summaryTxt;
}
