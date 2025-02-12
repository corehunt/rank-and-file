package com.rankandfile.dataloader.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "raf_congress")
@Data
public class Congress extends RAFAudit {

    @Id
    @Column(name = "congress_id")
    private Integer congressId;

    @Column(name = "congress_name")
    private String congressName;

    @Column(name = "congress_number", nullable = false)
    private Integer congressNumber;

    @Column(name = "start_year")
    private String startYear;

    @Column(name = "end_year")
    private String endYear;

    @OneToMany(mappedBy = "congress", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude // Exclude from hashCode and equals
    @ToString.Exclude // Exclude from toString
    @JsonManagedReference
    private Set<Session> sessions = new HashSet<>();

    @ManyToMany(mappedBy = "congresses")
    @EqualsAndHashCode.Exclude // Exclude from hashCode and equals
    @ToString.Exclude // Exclude from toString
    private Set<Person> members = new HashSet<>();

    // Override hashCode and equals based on congressId
    @Override
    public int hashCode() {
        return congressId != null ? congressId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Congress congress = (Congress) o;
        return congressId != null && congressId.equals(congress.congressId);
    }

}
