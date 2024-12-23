package com.rankandfile.backend.controller.internal;

import com.rankandfile.backend.dto.LeadershipDTO;
import com.rankandfile.backend.dto.PersonDTO;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.mapper.PersonMapper;
import com.rankandfile.backend.service.internal.PersonSearchDBService;
import com.rankandfile.backend.service.internal.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/internal")
@Controller("internalPersonController")
public class PersonController {

    private final PersonSearchDBService personSearchDBService;
    private final PersonService personService;
    private final PersonMapper personMapper;

    public PersonController(PersonSearchDBService personSearchDBService, PersonService personService, PersonMapper personMapper) {
        this.personSearchDBService = personSearchDBService;
        this.personService = personService;
        this.personMapper = personMapper;
    }

    @GetMapping("/{searchTerm}")
    public ResponseEntity<List<PersonDTO>> getAllPersons(@PathVariable String searchTerm) {
        log.info("searchTerm in getAllPersons Method: {}", searchTerm);
        List<Person> persons = personSearchDBService.getPersonListByFullName(searchTerm);
        List<PersonDTO> personDTOs = persons.stream()
                .map(personMapper::toPersonDTO)
                .collect(Collectors.toList());
        log.info("this is the persons response for the searchTerm: {}", persons);
        return ResponseEntity.ok(personDTOs);
    }

    @GetMapping("/politician/{personId}")
    public ResponseEntity<PersonDTO> getPersonFromId(@PathVariable String personId) {
        PersonDTO personDTO = personService.getPersonDTOById(personId);
        return ResponseEntity.ok(personDTO);
    }

    @GetMapping("/politician/leadership")
    public ResponseEntity<List<LeadershipDTO>> getLeadershipFromId() {
        List<LeadershipDTO> leadershipDTOList = personService.getCurrentLeadership();
        return ResponseEntity.ok(leadershipDTOList);
    }
}