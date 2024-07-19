package com.rankandfile.backend.controller.external;

import com.rankandfile.backend.entity.Congress;
import com.rankandfile.backend.service.CongressService;
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

    @GetMapping("/{congressNo}")
    public ResponseEntity<List<Congress>> fetchAndSaveCongress(@PathVariable String congressNo) {
        List<Congress> congresses = congressService.fetchAndSaveCongressByNumber(congressNo);
        return ResponseEntity.ok(congresses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Congress>> getAllCongresses() {
        List<Congress> congresses = congressService.getAllCongresses();
        return ResponseEntity.ok(congresses);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Congress> getCongressById(@PathVariable Integer id) {
        Congress congress = congressService.getCongressById(id);
        if (congress != null) {
            return ResponseEntity.ok(congress);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
