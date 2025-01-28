package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.TermDTO;
import com.rankandfile.dataloader.entity.Term;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TermMapper {

    TermDTO toTermDTO(Term term);
}
