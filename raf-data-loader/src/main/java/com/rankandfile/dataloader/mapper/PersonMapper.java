package com.rankandfile.dataloader.mapper;

import com.rankandfile.dataloader.dto.PersonDTO;
import com.rankandfile.dataloader.entity.Person;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TermMapper.class)
public interface PersonMapper {

    PersonDTO toPersonDTO(Person person);
}

