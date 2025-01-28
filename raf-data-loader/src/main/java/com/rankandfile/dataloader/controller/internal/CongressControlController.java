package com.rankandfile.dataloader.controller.internal;

import com.rankandfile.dataloader.dto.ChamberPartyCountDTO;
import com.rankandfile.dataloader.service.internal.CongressControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal")
@Controller("CongressMakeupController")
public class CongressControlController {

    private final CongressControlService controlService;

    public CongressControlController(CongressControlService controlService) {
        this.controlService = controlService;
    }


    @GetMapping("/control/{congress}")
    public List<ChamberPartyCountDTO> getChamberPartyCounts(@PathVariable Integer congress) {
        return controlService.getChamberPartyCount(congress);
    }
}
