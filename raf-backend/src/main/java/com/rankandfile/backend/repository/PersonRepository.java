package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonRepository extends JpaRepository<Person, String> {

    @Query("SELECT p FROM Person p WHERE p.personId = :personId")
    Person findPersonByPersonId(@Param("personId") String personId);
}
