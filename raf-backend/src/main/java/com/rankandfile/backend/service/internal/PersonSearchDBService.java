package com.rankandfile.backend.service.internal;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class PersonSearchDBService {

    private final PersonRepository personRepository;

    public PersonSearchDBService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }


    public List<Person> getPersonListByFullName(String searchTerm) {
        return personRepository.findPersonByFullNameSearchTerm(searchTerm);
    }

    public Person getPersonById(String personId) {
        return personRepository.findPersonByPersonId(personId);
    }

}
