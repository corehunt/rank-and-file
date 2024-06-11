package com.rankandfile.backend.controller;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public ResponseEntity<List<Person>> getAllPersons() {
        List<Person> persons = personService.getAllPersons();
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/{bioguideId}")
    public ResponseEntity<Person> getPerson(@PathVariable String bioguideId) {
        Person person = personService.fetchAndProcessPerson(bioguideId);
        return ResponseEntity.ok(person);
    }

    @PostMapping("/fetch/{bioguideId}")
    public ResponseEntity<String> fetchAndSavePerson(@PathVariable String bioguideId) {
        Person person = personService.fetchAndProcessPerson(bioguideId);
        personService.savePerson(person);
        return ResponseEntity.ok("Person fetched and saved successfully.");
    }
}