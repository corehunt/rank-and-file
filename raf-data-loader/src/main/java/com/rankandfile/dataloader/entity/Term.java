package com.rankandfile.dataloader.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

@Table(name = "raf_term")
@Entity
@Data
public class Term extends RAFAudit {

    @Id
    @Column(name = "term_id", nullable = false, updatable = false)
    private Integer termId;

    @ManyToOne
    @JoinColumn(name = "person_id")
    @JsonBackReference
    private Person person;

    @Column(name = "chamber")
    private String chamber;

    @Column(name = "congress")
    private Integer congress;

    @Column(name = "district")
    private Integer district;

    @Column(name = "end_year")
    private Integer endYr;

    @Column(name = "member_type")
    private String memberType;

    @Column(name = "start_year")
    private Integer startYr;

    @Column(name = "state_code")
    private String stateCd;

    @Column(name = "state_name")
    private String stateNm;

    @Override
    public String toString() {
        return "Term{" +
                "id=" + termId +
                ", chamber='" + chamber + '\'' +
                ", congress=" + congress +
                ", district=" + district +
                ", endYear=" + endYr +
                ", memberType='" + memberType + '\'' +
                ", startYear=" + startYr +
                ", stateCode='" + stateCd + '\'' +
                ", stateName='" + stateNm  + '\'' +
                '}';
    }
}
