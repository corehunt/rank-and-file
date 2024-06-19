package com.rankandfile.backend.util;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.repository.PersonRepository;
import lombok.Data;
import org.springframework.stereotype.Component;


@Data
@Component
public class Supplier {

    private PersonRepository personRepository;

    public Supplier(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person findOrCreatePerson(String personId) {
        return personRepository.findById(personId).orElse(new Person());
    }
}
