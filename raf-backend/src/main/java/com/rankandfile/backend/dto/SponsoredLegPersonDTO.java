package com.rankandfile.backend.dto;

import lombok.Data;

@Data
public class SponsoredLegPersonDTO {
    private String sponLegId;
    private PersonSummaryDTO person;
    private String sponsorType;
}
