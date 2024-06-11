package com.rankandfile.backend.entity.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
@EntityListeners(AuditEntityListener.class)
public class RAFAudit {

    @Column(name = "CREATE_TS", nullable = false, updatable = false)
    private String createTimestamp;

    @Column(name = "UPDATE_TS", nullable = false)
    private String updateTimestamp;

}
