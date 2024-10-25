package com.rankandfile.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "RAF_BILL")
@Entity
@Data
public class Bill extends RAFAudit {

    @Id
    @Column(name = "BILL_ID", nullable = false, updatable = false)
    private String billId;

    @Column(name = "BILL_NO")
    private Integer billNo;

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
    private Integer congress;

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

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("bill-sponsorship")
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

}
