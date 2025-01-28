package com.rankandfile.dataloader.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ActionDTO {
    private String actionId;
    private String actionCode;
    private LocalDate actionDate;
    private Integer sourceSystemCode;
    private String sourceSystemName;
    private String actionText;
    private String actionType;
    private String committeeRef;

}
