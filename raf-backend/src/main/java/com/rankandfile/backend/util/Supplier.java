package com.rankandfile.backend.util;

import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.repository.PersonRepository;
import lombok.Data;
import org.springframework.stereotype.Component;


@Data
@Component
public class Supplier {

    private final BillRepository billRepository;
    private PersonRepository personRepository;

    public Supplier(PersonRepository personRepository, BillRepository billRepository) {
        this.personRepository = personRepository;
        this.billRepository = billRepository;
    }

    public Person findOrCreatePerson(String personId) {
        return personRepository.findById(personId).orElse(new Person());
    }

}
