package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.Person;
import org.springframework.data.domain.Page;

public interface PersonSearchRepository {

    Page<Person> searchPeople(String parsedQuery, int page, int size);
}
