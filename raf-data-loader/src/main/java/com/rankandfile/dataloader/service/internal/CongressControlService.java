package com.rankandfile.dataloader.service.internal;

import com.rankandfile.dataloader.dto.ChamberPartyCountDTO;
import com.rankandfile.dataloader.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CongressControlService {

    private final PersonRepository personRepository;

    public List<ChamberPartyCountDTO> getChamberPartyCount(Integer congress) {
        // Fetch raw results from the repository
        List<Object[]> rows = personRepository.findChamberPartyCount(congress);

        // Convert each row into ChamberPartyCountDTO
        return rows.stream().map(row -> {
            // row[0] -> COUNT(1)
            // row[1] -> t.CHAMBER
            // row[2] -> p.PARTY

            Number countNumber = (Number) row[0];     // The raw "count" could be Long, BigInteger, etc.
            Integer count      = countNumber.intValue(); // Convert safely to int

            String chamber     = (String) row[1];
            String party       = (String) row[2];

            return new ChamberPartyCountDTO(count, chamber, party);
        }).collect(Collectors.toList());
    }
}
