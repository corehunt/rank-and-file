package com.rankandfile.backend.service.internal;

import com.rankandfile.backend.dto.LeadershipDTO;
import com.rankandfile.backend.dto.PersonDTO;
import com.rankandfile.backend.entity.Leadership;
import com.rankandfile.backend.entity.Person;
import com.rankandfile.backend.mapper.LeadershipMapper;
import com.rankandfile.backend.mapper.PersonMapper;
import com.rankandfile.backend.repository.LeadershipRepository;
import com.rankandfile.backend.repository.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final LeadershipRepository leadershipRepository;
    private final LeadershipMapper leadershipMapper;

    public PersonService(PersonRepository personRepository, PersonMapper personMapper, LeadershipRepository leadershipRepository, LeadershipMapper leadershipMapper) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.leadershipRepository = leadershipRepository;
        this.leadershipMapper = leadershipMapper;
    }

    public PersonDTO getPersonDTOById(String personId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));

        return personMapper.toPersonDTO(person);
    }

    public List<LeadershipDTO> getCurrentLeadership() {
        List<Leadership> leadershipList = leadershipRepository.findByCurrentLeader();
        if (leadershipList.isEmpty()) {
            throw new EntityNotFoundException("No current leadership found");
        }
        return leadershipMapper.toLeadershipDTOMapper(leadershipList);
    }
}
