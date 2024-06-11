package com.rankandfile.backend.entity.audit;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditEntityListener {

    @PrePersist
    public void prePersist(RAFAudit audit) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        audit.setCreateTimestamp(now);
        audit.setUpdateTimestamp(now);
    }

    @PreUpdate
    public void preUpdate(RAFAudit audit) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        audit.setUpdateTimestamp(now);
    }
}
