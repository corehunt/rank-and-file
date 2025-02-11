package com.rankandfile.dataloader.entity.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EntityListeners(AuditEntityListener.class)
public class RAFAudit {

    @Column(name = "create_ts", nullable = false, updatable = false)
    private LocalDateTime createTimestamp;

    @Column(name = "update_ts", nullable = false)
    private LocalDateTime updateTimestamp;

}
