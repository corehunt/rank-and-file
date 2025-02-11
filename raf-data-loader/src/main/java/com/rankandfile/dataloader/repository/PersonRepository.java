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
            t.chamber     AS chamber,
            p.party       AS party
        FROM raf_person p
        JOIN raf_term t ON p.person_id = t.person_id
        WHERE t.congress = :congress
        GROUP BY t.chamber, p.party
    """, nativeQuery = true)
    List<Object[]> findChamberPartyCount(@Param("congress") Integer congress);

    @Query("SELECT p.personId FROM Person p WHERE p.currentMember = 'Yes'")
    List<String> findAllCurrentMemberIds();

}
