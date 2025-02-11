package com.rankandfile.dataloader.entity;

import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "raf_committee", uniqueConstraints = @UniqueConstraint(columnNames = "sys_code"))
@Data
public class Committee extends RAFAudit {

    @Id
    @Column(name = "committee_id", nullable = false, updatable = false)
    private String committeeId;

    @Column(name = "chamber")
    private String chamber;

    @Column(name = "comm_type_cd")
    private String commTypeCd;

    @Column(name = "comm_name")
    private String commName;

    @Column(name = "sys_code", unique = true)
    private String sysCode;

    @Column(name = "url_src")
    private String urlSrc;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Committee parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Committee> subCommittees = new ArrayList<>();

    @ManyToMany(mappedBy = "committees")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Bill> bills = new ArrayList<>();
}
