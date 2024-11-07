package com.rankandfile.backend.service.internal;

import com.rankandfile.backend.dto.PersonDTO;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.mapper.PersonMapper;
import com.rankandfile.backend.repository.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonService(PersonRepository personRepository, PersonMapper personMapper) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    public PersonDTO getPersonDTOById(String personId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));

        return personMapper.toPersonDTO(person);
    }
}
