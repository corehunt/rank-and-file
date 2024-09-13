package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/persons")
@Controller("externalPersonController")
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

    @PostMapping("fetch/members/{congressId}")
    public ResponseEntity<String> fetchAndSaveMembersOfCongress(@PathVariable String congressId) {
        List<Person> congressPersonList = personService.fetchMembersOfCurrentCongress(congressId);
        for (Person member : congressPersonList){
            personService.savePerson(member);
            log.info("successfully saved member: {}");
        }
        return ResponseEntity.ok("Successfully fetched and saved members of Congress.");
    }

    @PostMapping("fetch/members/{bioguideId}/sponsored-legislation")
    public ResponseEntity<List<SponsoredLegislation>> getSponsoredLegislationByPerson(@PathVariable String bioguideId) {
        List<SponsoredLegislation> legislationList = personService.getSponsoredLegislationByPersonId(bioguideId);
        return ResponseEntity.ok(legislationList);
    }
}