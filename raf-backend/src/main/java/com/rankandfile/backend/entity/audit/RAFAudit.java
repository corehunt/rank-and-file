package com.rankandfile.backend.entity.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EntityListeners(AuditEntityListener.class)
public class RAFAudit {

    @Column(name = "CREATE_TS", nullable = false, updatable = false)
    private LocalDateTime createTimestamp;

    @Column(name = "UPDATE_TS", nullable = false)
    private LocalDateTime updateTimestamp;

}
