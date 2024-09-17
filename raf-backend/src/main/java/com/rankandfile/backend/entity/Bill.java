package com.rankandfile.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

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

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SponsoredLegislation> sponsorships = new ArrayList<>();
}
