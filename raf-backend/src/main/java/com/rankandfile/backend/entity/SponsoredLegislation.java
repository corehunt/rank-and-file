package com.rankandfile.backend.entity;

import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "RAF_MEM_SPONS_LEGISLATION")
@Data
public class SponsoredLegislation extends RAFAudit {

    @Id
    @Column(name = "SPON_LEG_ID")
    private String sponLegId;

    @Column(name = "CONGRESS")
    private Integer congress;

    @Column(name = "INTRO_DT")
    private LocalDate introDt;

    @Column(name = "LATEST_ACTION_DT")
    private LocalDate latestActionDt;

    @Column(name = "LATEST_ACTION_TXT")
    private String latestActionTxt;

    @Column(name = "BILL_NO")
    private Integer billNo;

    @Column(name = "BILL_TYPE")
    private String billType;

    @Column(name = "POLICY_AREA")
    private String policyArea;

    @Column(name = "LEG_TITLE")
    private String legTitle;

    @Column(name = "LEG_TEXT")
    private String legTxt;

    @Column(name = "URL_SRC")
    private String urlSrc;

    @ManyToOne
    @JoinColumn(name = "PERSON_ID", nullable = false)
    private Person person;
    
}
