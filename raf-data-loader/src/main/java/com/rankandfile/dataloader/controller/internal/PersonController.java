package com.rankandfile.dataloader.controller.internal;

import com.rankandfile.dataloader.dto.LeadershipDTO;
import com.rankandfile.dataloader.dto.PersonDTO;
import com.rankandfile.dataloader.service.internal.PersonSearchDBService;
import com.rankandfile.dataloader.service.internal.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    private final PersonService personService;

    public PersonController(PersonSearchDBService personSearchDBService, PersonService personService) {
        this.personSearchDBService = personSearchDBService;
        this.personService = personService;
    }

    @GetMapping("/politicians/search")
    public ResponseEntity<Page<PersonDTO>> getAllPersons(
            @RequestParam("q") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<PersonDTO> persons = personSearchDBService.searchPersonsPaged(query, page, size);
        return ResponseEntity.ok(persons);
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