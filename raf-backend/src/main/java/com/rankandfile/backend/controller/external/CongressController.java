package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.entity.Congress;
import com.rankandfile.backend.service.external.congress.CongressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/congress")
public class CongressController {

    private final CongressService congressService;

    @Autowired
    public CongressController(CongressService congressService) {
        this.congressService = congressService;
    }

    //This controller is used to save Congress data and its sessions based off single congress number
    //api.congress.gov endpoint: /congress/{congress}
    @GetMapping("/{congressNo}")
    public ResponseEntity<List<Congress>> fetchAndSaveCongress(@PathVariable String congressNo) {
        List<Congress> congresses = congressService.fetchAndSaveCongressByNumber(congressNo);
        return ResponseEntity.ok(congresses);
    }

}
