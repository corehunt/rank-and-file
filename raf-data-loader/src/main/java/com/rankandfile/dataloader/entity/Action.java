package com.rankandfile.dataloader.entity;

import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "RAF_BILL_ACTION")
@Data
public class Action extends RAFAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACTION_ID", nullable = false, updatable = false)
    private Long actionId;

    @Column(name = "ACTION_CD")
    private String actionCode;

    @Column(name = "ACTION_DT")
    private LocalDate actionDate;

    @Column(name = "SRC_SYS_CD")
    private Integer sourceSystemCode;

    @Column(name = "SRC_NM")
    private String sourceSystemName;

    @Column(name = "ACTION_TXT", length = 1000)
    private String actionText;

    @Column(name = "ACTION_TYPE", length = 40)
    private String actionType;

    @ManyToOne
    @JoinColumn(name = "BILL_ID", nullable = false)
    private Bill bill;

    @Column(name = "COMMITTEE_REF", length = 2000)
    private String committeeRef;

    @Transient
    public Map<String, String> getCommitteeMap() {
        return parseCommitteeRef(committeeRef);
    }

    private Map<String, String> parseCommitteeRef(String ref) {
        Map<String, String> map = new HashMap<>();
        if (ref != null && !ref.isEmpty()) {
            String[] entries = ref.split(";");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        }
        return map;
    }

    public void setCommitteeMap(Map<String, String> committeeMap) {
        if (committeeMap == null || committeeMap.isEmpty()) {
            this.committeeRef = null;
        } else {
            this.committeeRef = committeeMap.entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .collect(Collectors.joining(";"));
        }
    }
}