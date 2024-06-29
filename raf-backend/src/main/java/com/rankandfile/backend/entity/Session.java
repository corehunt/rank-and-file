package com.rankandfile.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "RAF_SESSION")
@Data
public class Session extends RAFAudit {

    @Id
    @Column(name = "SESSION_ID")
    private Integer sessionId;

    @Column(name = "CHAMBER", nullable = false)
    private String chamber;

    @Column(name = "NUMBER", nullable = false)
    private Integer number;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONGRESS_ID")
    @EqualsAndHashCode.Exclude // Exclude from hashCode and equals
    @ToString.Exclude // Exclude from toString
    @JsonBackReference
    private Congress congress;

    // Override hashCode and equals based on sessionId
    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return sessionId != null && sessionId.equals(session.sessionId);
    }
}
