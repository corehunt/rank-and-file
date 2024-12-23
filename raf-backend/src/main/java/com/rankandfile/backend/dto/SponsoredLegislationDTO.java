package com.rankandfile.backend.dto;

import lombok.Data;

@Data
public class SponsoredLegislationDTO {
    private String sponLegId;
    private PersonSummaryDTO person;
    private String sponsorType;
    private BillDTO bill;
}
