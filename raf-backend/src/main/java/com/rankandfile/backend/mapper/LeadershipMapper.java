package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.LeadershipDTO;
import com.rankandfile.backend.entity.Leadership;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeadershipMapper {


    List<LeadershipDTO> toLeadershipDTOMapper(List<Leadership> leadershipList);
}
