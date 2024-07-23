package com.rankandfile.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

@Table(name = "RAF_TERM")
@Entity
@Data
public class Term extends RAFAudit {

    @Id
    @Column(name = "TERM_ID", nullable = false, updatable = false)
    private Integer termId;

    @ManyToOne
    @JoinColumn(name = "PERSON_ID")
    @JsonBackReference
    private Person person;

    @Column(name = "CHAMBER")
    private String chamber;

    @Column(name = "CONGRESS")
    private Integer congress;

    @Column(name = "DISTRICT")
    private Integer district;

    @Column(name = "END_YEAR")
    private Integer endYr;

    @Column(name = "MEMBER_TYPE")
    private String memberType;

    @Column(name = "START_YEAR")
    private Integer startYr;

    @Column(name = "STATE_CODE")
    private String stateCd;

    @Column(name = "STATE_NAME")
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
