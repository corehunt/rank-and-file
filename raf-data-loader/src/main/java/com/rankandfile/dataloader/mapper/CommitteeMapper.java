package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.CommitteeDTO;
import com.rankandfile.dataloader.entity.Committee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommitteeMapper {

    CommitteeDTO toCommitteeDTO(Committee committee);
}
