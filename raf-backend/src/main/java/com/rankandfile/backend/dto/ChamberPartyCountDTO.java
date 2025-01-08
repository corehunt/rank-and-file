package com.rankandfile.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChamberPartyCountDTO {
    private Integer count;
    private String chamber;
    private String party;
}
