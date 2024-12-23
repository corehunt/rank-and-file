package com.rankandfile.backend.dto;

import lombok.Data;

@Data
public class PersonSummaryDTO {
    private String personId;
    private String firstName;
    private String midName;
    private String lastName;
    private String fullName;
    private String state;
    private Integer currentDistrict;
    private String imageUrl;
    private String partyMembership;
}
