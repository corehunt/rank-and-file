package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.TermDTO;
import com.rankandfile.backend.entity.Term;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TermMapper {

    TermDTO toTermDTO(Term term);
}
