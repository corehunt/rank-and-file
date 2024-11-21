package com.rankandfile.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PersonDTO {
    private String personId;
    private String firstName;
    private String midName;
    private String lastName;
    private String fullName;
    private String birthDate;
    private String deathDate;
    private String website;
    private String officeLocLine1;
    private String officeLocLine2;
    private String phoneNo;
    private String state;
    private Integer currentDistrict;
    private String currentMember;
    private String biography;
    private String email;
    private String imageUrl;
    private String imgAttribution;
    private String partyMembership;
    private Integer partyStartYr;
    private List<TermDTO> termList;
}
