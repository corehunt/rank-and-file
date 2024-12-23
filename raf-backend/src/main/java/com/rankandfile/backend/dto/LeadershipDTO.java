package com.rankandfile.backend.dto;

import lombok.Data;

@Data
public class LeadershipDTO {
    private String leadershipId;
    private String leadershipType;
    private String currentLeader;
    private PersonSummaryDTO person;
}
