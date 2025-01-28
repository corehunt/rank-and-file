package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.LeadershipDTO;
import com.rankandfile.dataloader.entity.Leadership;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeadershipMapper {


    List<LeadershipDTO> toLeadershipDTOMapper(List<Leadership> leadershipList);
}
