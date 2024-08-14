package com.rankandfile.backend.entity;

import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "RAF_BILL_ACTION")
@Data
public class Action extends RAFAudit {

    @Id
    @Column(name = "ACTION_ID")
    private String actionId;

    @Column(name = "ACTION_CD")
    private String actionCode;

    @Column(name = "ACTION_DT")
    private LocalDate actionDate;

    @Column(name = "SRC_SYS_CD")
    private Integer sourceSystemCode;

    @Column(name = "SRC_NM")
    private String sourceSystemName;

    @Column(name = "ACTION_TXT")
    private String actionText;

    @Column(name = "ACTION_TYPE")
    private String actionType;

    @ManyToOne
    @JoinColumn(name = "BILL_ID", nullable = false)
    private Bill bill;
}