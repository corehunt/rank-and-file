package com.rankandfile.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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
    private String legislativeSubjects;
    private List<ActionDTO> actions;
    private List<SponsoredLegPersonDTO> sponsorships;
    private List<TextDTO> billTexts;
}
