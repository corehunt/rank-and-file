package com.rankandfile.backend.controller.internal;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal")
@Controller("internalPersonController")
public class PersonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/{searchTerm}")
    public ResponseEntity<List<Person>> getAllPersons(@PathVariable String searchTerm) {
        LOGGER.info("searchTerm in getAllPersons Method: {}", searchTerm);
        List<Person> persons = personService.getPersonListByFullName(searchTerm);
        LOGGER.info("this is the persons response for the searchTerm: {}", persons);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/politician/{personId}")
    public ResponseEntity<Person> getPersonFromId(@PathVariable String personId) {
        LOGGER.info("searchTerm in getPersonFromId Method: {}", personId);
        Person person = personService.getPersonById(personId);
        LOGGER.info("this is the persons response for the searchTerm: {}", person);
        return ResponseEntity.ok(person);
    }
}