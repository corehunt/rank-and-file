package com.rankandfile.dataloader.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.*;

@Table(name = "raf_person")
@Entity
@Data
public class Person extends RAFAudit {

    @Id
    @Column(name = "person_id", nullable = false, updatable = false)
    private String personId;

    @Column(name = "first_nm", nullable = false)
    private String firstName;

    @Column(name = "mid_nm")
    private String midName;

    @Column(name = "last_nm", nullable = false)
    private String lastName;

    @Column(name = "full_nm")
    private String fullName;

    @Column(name = "birth_dt")
    private LocalDate birthDate;

    @Column(name = "dth_dt")
    private LocalDate deathDate;

    @Column(name = "website")
    private String website;

    @Column(name = "office_loc_ln1")
    private String officeLocLine1;

    @Column(name = "office_loc_ln2")
    private String officeLocLine2;

    @Column(name = "phone")
    private String phoneNo;

    @Column(name = "state")
    private String state;

    @Column(name = "state_abbr")
    private String stateAbbr;

    @Column(name = "current_district")
    private Integer currentDistrict;

    @Column(name = "current_mem")
    private String currentMember;

    @Column(name = "bio")
    private String biography;

    @Column(name = "email")
    private String email;

    @Column(name = "img_url")
    private String imageUrl;

    @Column(name = "img_attribution")
    private String imgAttribution;

    @Column(name = "party_mem")
    private String partyMembership;

    @Column(name = "party")
    private String party;

    @Column(name = "party_st_yr")
    private Integer partyStartYr;

    @ManyToMany
    @JoinTable(
            name = "raf_person_congress",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "congress_id")
    )
    @JsonIgnore
    private Set<Congress> congresses = new HashSet<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Term> termList = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Leadership> leadershipList = new ArrayList<>();


    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("person-sponsorship")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SponsoredLegislation> sponsoredLegislationList = new ArrayList<>();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return Objects.equals(personId, person.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId);
    }
}
