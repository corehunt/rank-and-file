package com.rankandfile.dataloader.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "raf_bill")
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Bill extends RAFAudit {

    @Id
    @Column(name = "bill_id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private String billId;

    @Column(name = "bill_no")
    private String billNo;

    @Column(name = "bill_title")
    private String billTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "introduced_dt")
    private LocalDate introducedDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "latest_action_dt")
    private LocalDate latestActionDt;

    @Column(name = "latest_action_txt")
    private String latestActionTxt;

    @Column(name = "policy_area")
    private String policyArea;

    @Column(name = "congress")
    private String congress;

    @Column(name = "bill_type")
    private String billType;

    @Column(name = "origin_chamber")
    private String originChamber;

    @Column(name = "origin_chamber_cd")
    private String originChamberCd;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "summary_action_dt")
    private LocalDate summaryActionDt;

    @Column(name = "summary_action_desc")
    private String summaryActionDesc;

    @Column(name = "summary_txt", columnDefinition = "TEXT")
    private String summaryTxt;

    @Column(name = "law_no")
    private String lawNo;

    @Column(name = "law_type")
    private String lawType;

    @Column(name = "is_law_fl")
    private String isLawFl;

    @Column(name = "legislative_subjects", columnDefinition = "TEXT")
    private String legislativeSubjects;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("sponsorship-bill")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SponsoredLegislation> sponsorships = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "raf_bill_committee",
            joinColumns = @JoinColumn(name = "bill_id"),
            inverseJoinColumns = @JoinColumn(name = "committee_id")
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
            name = "raf_bill_related_bills",
            joinColumns = @JoinColumn(name = "bill_id"),
            inverseJoinColumns = @JoinColumn(name = "related_bill_id")
    )
    @ToString.Exclude
    private Set<Bill> relatedBills = new HashSet<>();
}
