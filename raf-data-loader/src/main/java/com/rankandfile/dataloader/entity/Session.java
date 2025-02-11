package com.rankandfile.dataloader.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "raf_session")
@Data
public class Session extends RAFAudit {

    @Id
    @Column(name = "session_id")
    private Integer sessionId;

    @Column(name = "chamber", nullable = false)
    private String chamber;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "type")
    private String type;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "congress_id")
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
