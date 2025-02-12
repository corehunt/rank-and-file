package com.rankandfile.dataloader.entity;

import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "raf_bill_action")
@Data
public class Action extends RAFAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id", nullable = false, updatable = false)
    private Long actionId;

    @Column(name = "action_cd")
    private String actionCode;

    @Column(name = "action_dt")
    private LocalDate actionDate;

    @Column(name = "src_sys_cd")
    private Integer sourceSystemCode;

    @Column(name = "src_nm")
    private String sourceSystemName;

    @Column(name = "action_txt", length = 1000)
    private String actionText;

    @Column(name = "action_type", length = 40)
    private String actionType;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(name = "committee_ref", length = 2000)
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