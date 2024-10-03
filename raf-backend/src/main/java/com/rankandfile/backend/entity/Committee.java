package com.rankandfile.backend.entity;

import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RAF_COMMITTEE", uniqueConstraints = @UniqueConstraint(columnNames = "SYS_CODE"))
@Data
public class Committee extends RAFAudit {

    @Id
    @Column(name = "COMMITTEE_ID", nullable = false, updatable = false)
    private String committeeId;

    @Column(name = "CHAMBER")
    private String chamber;

    @Column(name = "COMM_TYPE_CD")
    private String commTypeCd;

    @Column(name = "COMM_NAME")
    private String commName;

    @Column(name = "SYS_CODE", unique = true)
    private String sysCode;

    @Column(name = "URL_SRC")
    private String urlSrc;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Committee parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Committee> subCommittees = new ArrayList<>();

    @ManyToMany(mappedBy = "committees")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Bill> bills = new ArrayList<>();
}
