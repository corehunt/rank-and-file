package com.rankandfile.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommitteeDTO {
    private String committeeId;
    private String chamber;
    private String commTypeCd;
    private String commName;
    private String sysCode;
}
