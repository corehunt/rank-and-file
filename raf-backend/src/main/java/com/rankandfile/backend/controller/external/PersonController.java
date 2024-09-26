package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.entity.SponsoredLegislation;
import com.rankandfile.backend.service.external.person.CongressClassPersonService;
import com.rankandfile.backend.service.external.person.MemberCoSponsLegislationService;
import com.rankandfile.backend.service.external.person.MemberService;
import com.rankandfile.backend.service.external.person.MemberSponsLegislationService;
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

    private final int LIMIT = 250;
    private final MemberService memberService;
    private final CongressClassPersonService congressClassPersonService;
    private final MemberSponsLegislationService memberSponsLegislationService;
    private final MemberCoSponsLegislationService memberCoSponsLegislationService;

    public PersonController(MemberService memberService, CongressClassPersonService congressClassPersonService, MemberSponsLegislationService memberSponsLegislationService, MemberCoSponsLegislationService memberCoSponsLegislationService) {
        this.memberService = memberService;
        this.congressClassPersonService = congressClassPersonService;
        this.memberSponsLegislationService = memberSponsLegislationService;
        this.memberCoSponsLegislationService = memberCoSponsLegislationService;
    }

    //implement controller to get every member
//    @GetMapping
//    public ResponseEntity<List<Person>> getAllPersons() {
//        return ResponseEntity.ok(persons);
//    }

    //This controller is used to load a single person/member based off bioguideId obtained from congress.gov api
    //api.congress.gov endpoint: /member/{bioguideId}
    @PostMapping("/fetch/{bioguideId}")
    public ResponseEntity<String> fetchAndSavePerson(@PathVariable String bioguideId) {
        memberService.fetchAndProcessPerson(bioguideId);
        return ResponseEntity.ok("Person fetched and saved successfully.");
    }

    //This controller is used to load all members in a given congress, given the congress number
    //api.congress.gov endpoint: /member/congress/{congress}
    @PostMapping("fetch/members/{congressId}")
    public ResponseEntity<String> fetchAndSaveMembersOfCongress(@PathVariable String congressId) {
        congressClassPersonService.fetchMembersOfCurrentCongress(congressId, LIMIT);
        return ResponseEntity.ok("Successfully fetched and saved members of Congress.");
    }

    //This controller is used to load all the given sponsored legislation given a members bioguideId
    //api.congress.gov endpoint: /member/{bioguideId}/sponsored-legislation
    @PostMapping("fetch/members/{bioguideId}/sponsored-legislation")
    public ResponseEntity<List<SponsoredLegislation>> getSponsoredLegislationByPerson(@PathVariable String bioguideId) {
        List<SponsoredLegislation> legislationList = memberSponsLegislationService.getSponsoredLegislationByPersonId(bioguideId, LIMIT);
        return ResponseEntity.ok(legislationList);
    }

    //This controller is used to load all the given co-sponsored legislation given a members bioguideId
    //api.congress.gov endpoint: /member/{bioguideId}/cosponsored-legislation
    @PostMapping("fetch/members/{bioguideId}/cosponsored-legislation")
    public ResponseEntity<List<SponsoredLegislation>> getCoSponsoredLegislationByPerson(@PathVariable String bioguideId) {
        List<SponsoredLegislation> legislationList = memberCoSponsLegislationService.getCoSponsoredLegislationByPersonId(bioguideId, LIMIT);
        return ResponseEntity.ok(legislationList);
    }
}