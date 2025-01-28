package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.ActionDTO;
import com.rankandfile.dataloader.entity.Action;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActionMapper {

    ActionDTO toActionDTO(Action action);
}
