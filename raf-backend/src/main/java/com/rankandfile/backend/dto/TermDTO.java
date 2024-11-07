package com.rankandfile.backend.dto;

import lombok.Data;

@Data
public class TermDTO {
    private Long termId;
    private String chamber;
    private Integer congress;
    private Integer district;
    private Integer startYr;
    private Integer endYr;
    private String memberType;
    private String stateCd;
    private String stateNm;
}
