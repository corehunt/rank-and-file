package com.rankandfile.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "RAF_BILL")
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Bill extends RAFAudit {

    @Id
    @Column(name = "BILL_ID", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private String billId;

    @Column(name = "BILL_NO")
    private String billNo;

    @Column(name = "BILL_TITLE")
    private String billTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "INTRODUCED_DT")
    private LocalDate introducedDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "LATEST_ACTION_DT")
    private LocalDate latestActionDt;

    @Column(name = "LATEST_ACTION_TXT")
    private String latestActionTxt;

    @Column(name = "POLICY_AREA")
    private String policyArea;

    @Column(name = "CONGRESS")
    private String congress;

    @Column(name = "BILL_TYPE")
    private String billType;

    @Column(name = "ORIGIN_CHAMBER")
    private String originChamber;

    @Column(name = "ORIGIN_CHAMBER_CD")
    private String originChamberCd;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "SUMMARY_ACTION_DT")
    private LocalDate summaryActionDt;

    @Column(name = "SUMMARY_ACTION_DESC")
    private String summaryActionDesc;

    @Column(name = "SUMMARY_TXT", columnDefinition = "TEXT")
    private String summaryTxt;

    @Column(name = "LAW_NO")
    private String lawNo;

    @Column(name = "LAW_TYPE")
    private String lawType;

    @Column(name = "IS_LAW_FL")
    private String isLawFl;

    @Column(name = "LEGISLATIVE_SUBJECTS", columnDefinition = "TEXT")
    private String legislativeSubjects;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("sponsorship-bill")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SponsoredLegislation> sponsorships = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "RAF_BILL_COMMITTEE",
            joinColumns = @JoinColumn(name = "BILL_ID"),
            inverseJoinColumns = @JoinColumn(name = "COMMITTEE_ID")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Committee> committees = new ArrayList<>();

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Text> billTexts = new ArrayList<>();

    // New relationship with actions
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonBackReference
    private List<Action> actions = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "RAF_BILL_RELATED_BILLS",
            joinColumns = @JoinColumn(name = "BILL_ID"),
            inverseJoinColumns = @JoinColumn(name = "RELATED_BILL_ID")
    )
    @ToString.Exclude
    private Set<Bill> relatedBills = new HashSet<>();
}
