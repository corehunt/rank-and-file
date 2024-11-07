package com.rankandfile.backend.mapper;

import com.rankandfile.backend.dto.PersonDTO;
import com.rankandfile.backend.entity.Person;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TermMapper.class)
public interface PersonMapper {

    PersonDTO toPersonDTO(Person person);
}

