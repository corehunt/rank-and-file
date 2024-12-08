package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.ActionDTO;
import com.rankandfile.backend.entity.Action;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActionMapper {

    ActionDTO toActionDTO(Action action);
}
