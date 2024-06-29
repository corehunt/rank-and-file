package com.rankandfile.backend.entity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "RAF_STATE_DOMN")
@Data
public class StateDomain {

    @Id
    @Column(name = "STATE_ID")
    private Integer stateId;

    @Column(name = "STATE_ABBR")
    private String stateAbbr;

    @Column(name = "STATE_NM")
    private String stateNm;

    @Column(name = "CAPITAL")
    private String capital;

}
