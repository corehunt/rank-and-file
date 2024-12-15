package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.TextDTO;
import com.rankandfile.backend.entity.Text;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TermMapper.class)
public interface TextMapper {

    TextDTO toTextDTO(Text text);
}
