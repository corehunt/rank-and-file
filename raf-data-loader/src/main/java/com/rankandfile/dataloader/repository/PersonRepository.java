package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, String> {

    @Query("SELECT p FROM Person p WHERE p.personId = :personId")
    Person findPersonByPersonId(@Param("personId") String personId);

    @Query(value = """
        SELECT
            COUNT(1)      AS count,
            t.CHAMBER     AS chamber,
            p.PARTY       AS party
        FROM RAF_PERSON p
        JOIN RAF_TERM t ON p.PERSON_ID = t.PERSON_ID
        WHERE t.CONGRESS = :congress
        GROUP BY t.CHAMBER, p.PARTY
    """, nativeQuery = true)
    List<Object[]> findChamberPartyCount(@Param("congress") Integer congress);

    @Query("SELECT p.personId FROM Person p WHERE p.currentMember = 'Yes'")
    List<String> findAllCurrentMemberIds();

}
