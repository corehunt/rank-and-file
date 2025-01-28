package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.TextDTO;
import com.rankandfile.dataloader.entity.Text;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TermMapper.class)
public interface TextMapper {

    TextDTO toTextDTO(Text text);
}
