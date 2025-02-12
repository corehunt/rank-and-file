package com.rankandfile.dataloader.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

@Table(name = "raf_leadership")
@Entity
@Data
public class Leadership extends RAFAudit {

    @Id
    @Column(name = "leadership_id", nullable = false, updatable = false)
    private Integer leadershipId;

    @ManyToOne
    @JoinColumn(name = "person_id")
    @JsonBackReference
    private Person person;

    @Column(name = "congress")
    private String congress;

    @Column(name = "leadership_type")
    private String leadershipType;

    @Column(name = "current_leader")
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
