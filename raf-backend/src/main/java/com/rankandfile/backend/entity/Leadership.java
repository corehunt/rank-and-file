package com.rankandfile.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

@Table(name = "RAF_LEADERSHIP")
@Entity
@Data
public class Leadership extends RAFAudit {

    @Id
    @Column(name = "LEADERSHIP_ID", nullable = false, updatable = false)
    private Integer leadershipId;

    @ManyToOne
    @JoinColumn(name = "PERSON_ID")
    @JsonBackReference
    private Person person;

    @Column(name = "CONGRESS")
    private String congress;

    @Column(name = "LEADERSHIP_TYPE")
    private String leadershipType;

    @Column(name = "CURRENT_LEADER")
    private String currentLeader;

    @Override
    public String toString() {
        return "Leadership{" +
                "leadershipId=" + leadershipId +
                ", congress='" + congress + '\'' +
                ", leadershipType='" + leadershipType + '\'' +
                ", currentLeader='" + currentLeader + '\'' +
                '}';
    }
}
