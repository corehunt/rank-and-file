package com.rankandfile.backend.entity;

import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Table(name = "RAF_PERSON")
@Entity
@Data
public class Person extends RAFAudit {

    @Id
    @Column(name = "PERSON_ID", nullable = false, updatable = false)
    private String personId;

    @Column(name = "FIRST_NM")
    private String firstName;

    @Column(name = "MID_NM")
    private String midName;

    @Column(name = "LAST_NM")
    private String lastName;

    @Column(name = "BIRTH_DT")
    private LocalDate birthDate;

    @Column(name = "DTH_DT")
    private LocalDate deathDate;

    @Column(name = "WEBSITE")
    private String website;

    @Column(name = "OFFICE_LOC_LN1")
    private String officeLocLine1;

    @Column(name = "OFFICE_LOC_LN2")
    private String officeLocLine2;

    @Column(name = "PHONE")
    private String phoneNo;

    @Column(name = "STATE")
    private String state;

    @Column(name = "CURRENT_DISTRICT")
    private Integer currentDistrict;

    @Column(name = "BIO")
    private String biography;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "IMG_URL")
    private String imageUrl;

    @Column(name = "IMG_ATTRIBUTION")
    private String imgAttribution;

    @Column(name = "PARTY_MEM")
    private String partyMembership;

    @Column(name = "PARTY_ST_YR")
    private Integer partyStartYr;

}
