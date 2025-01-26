package com.rankandfile.backend.entity.audit;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

public class AuditEntityListener {

    @PrePersist
    public void prePersist(RAFAudit audit) {
        LocalDateTime now = LocalDateTime.now();
        audit.setCreateTimestamp(now);
        audit.setUpdateTimestamp(now);
    }

    @PreUpdate
    public void preUpdate(RAFAudit audit) {
        LocalDateTime now = LocalDateTime.now();
        audit.setUpdateTimestamp(now);
    }
}
