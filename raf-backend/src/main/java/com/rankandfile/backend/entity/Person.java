package com.rankandfile.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(name = "FULL_NM")
    private String fullName;

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

    @Column(name = "CURRENT_MEM")
    private String currentMember;

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

    @ManyToMany
    @JoinTable(
            name = "RAF_PERSON_CONGRESS",
            joinColumns = @JoinColumn(name = "PERSON_ID"),
            inverseJoinColumns = @JoinColumn(name = "CONGRESS_ID")
    )
    @JsonIgnore
    private Set<Congress> congresses = new HashSet<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Term> termList = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SponsoredLegislation> sponsoredLegislationList;

    @Override
    public String toString() {
        return "Person{" +
                "personId='" + personId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", midName='" + midName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", birthDate=" + birthDate +
                ", deathDate=" + deathDate +
                ", website='" + website + '\'' +
                ", officeLocLine1='" + officeLocLine1 + '\'' +
                ", officeLocLine2='" + officeLocLine2 + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", state='" + state + '\'' +
                ", currentDistrict=" + currentDistrict +
                ", biography='" + biography + '\'' +
                ", email='" + email + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", imgAttribution='" + imgAttribution + '\'' +
                ", partyMembership='" + partyMembership + '\'' +
                ", partyStartYr=" + partyStartYr +
                '}';
    }
}
