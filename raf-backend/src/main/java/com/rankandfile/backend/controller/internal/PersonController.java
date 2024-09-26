package com.rankandfile.backend.controller.internal;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.service.internal.PersonSearchDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal")
@Controller("internalPersonController")
public class PersonController {

    private final PersonSearchDBService personSearchDBService;

    public PersonController(PersonSearchDBService personSearchDBService) {
        this.personSearchDBService = personSearchDBService;
    }

    @GetMapping("/{searchTerm}")
    public ResponseEntity<List<Person>> getAllPersons(@PathVariable String searchTerm) {
        log.info("searchTerm in getAllPersons Method: {}", searchTerm);
        List<Person> persons = personSearchDBService.getPersonListByFullName(searchTerm);
        log.info("this is the persons response for the searchTerm: {}", persons);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/politician/{personId}")
    public ResponseEntity<Person> getPersonFromId(@PathVariable String personId) {
        log.info("searchTerm in getPersonFromId Method: {}", personId);
        Person person = personSearchDBService.getPersonById(personId);
        log.info("this is the persons response for the searchTerm: {}", person);
        return ResponseEntity.ok(person);
    }
}