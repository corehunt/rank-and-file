package com.rankandfile.backend.controller.internal;

import com.rankandfile.backend.dto.SponsoredLegislationDTO;
import com.rankandfile.backend.service.internal.SponsoredLegislationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal")
public class SponsoredLegislationController {

    private final SponsoredLegislationService sponsoredLegislationService;

    public SponsoredLegislationController(SponsoredLegislationService sponsoredLegislationService) {
        this.sponsoredLegislationService = sponsoredLegislationService;
    }

    @GetMapping("/politician/{personId}/sponsored")
    public ResponseEntity<Page<SponsoredLegislationDTO>> getSponsoredLegislation(
            @PathVariable String personId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SponsoredLegislationDTO> sponsoredLegislation =
                sponsoredLegislationService.getSponsoredLegislation(personId, page, size);
        return ResponseEntity.ok(sponsoredLegislation);
    }

    @GetMapping("/politician/{personId}/cosponsored")
    public ResponseEntity<Page<SponsoredLegislationDTO>> getCoSponsoredLegislation(
            @PathVariable String personId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SponsoredLegislationDTO> coSponsoredLegislation =
                sponsoredLegislationService.getCoSponsoredLegislation(personId, page, size);
        return ResponseEntity.ok(coSponsoredLegislation);
    }
}

