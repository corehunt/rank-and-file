package com.rankandfile.dataloader.service.internal;

import com.rankandfile.dataloader.dto.PersonDTO;
import com.rankandfile.dataloader.entity.Person;
import com.rankandfile.dataloader.mapper.PersonMapper;
import com.rankandfile.dataloader.repository.PersonSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class PersonSearchDBService {

    private final PersonSearchRepository personSearchRepository;
    private final PersonMapper personMapper;

    public PersonSearchDBService(PersonSearchRepository personSearchRepository, PersonMapper personMapper) {
        this.personSearchRepository = personSearchRepository;
        this.personMapper = personMapper;
    }

    public Page<PersonDTO> searchPersonsPaged(String rawQuery, int page, int size) {
        String parsedQuery = buildBooleanQuery(rawQuery);
        if (parsedQuery.isEmpty()) {
            // Return an empty page if there's no valid query
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Person> personPage = personSearchRepository.searchPeople(parsedQuery, page, size);

        return personPage.map(personMapper::toPersonDTO);
    }


    /**
     * Builds a MySQL boolean-mode query for partial matching.
     */
    private String buildBooleanQuery(String raw) {
        if (raw == null || raw.isBlank()) return "";

        String[] tokens = raw.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (!token.isBlank()) {
                sb.append("+").append(token).append("* ");
            }
        }
        return sb.toString().trim();
    }
}
