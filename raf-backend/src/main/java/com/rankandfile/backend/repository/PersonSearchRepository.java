package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Person;
import org.springframework.data.domain.Page;

public interface PersonSearchRepository {

    Page<Person> searchPeople(String parsedQuery, int page, int size);
}
