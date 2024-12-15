package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.CommitteeDTO;
import com.rankandfile.backend.entity.Committee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommitteeMapper {

    CommitteeDTO toCommitteeDTO(Committee committee);
}
