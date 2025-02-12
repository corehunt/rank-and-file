package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PersonSearchRepositoryImpl implements PersonSearchRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Person> searchPeople(String parsedQuery, int page, int size) {
        int offset = page * size;

        // 1) Count query
        String countSql = """
            SELECT COUNT(*)
            FROM raf_person p
            WHERE MATCH (p.first_nm, p.last_nm, p.full_nm, p.state, p.state_abbr, p.party_mem, p.party)
                  AGAINST (:parsed IN BOOLEAN MODE)
            """;

        Number countResult = (Number) em.createNativeQuery(countSql)
                .setParameter("parsed", parsedQuery)
                .getSingleResult();
        long totalElements = countResult.longValue();

        // 2) Main query for content
        String sql = """
            SELECT p.*
            FROM raf_person p
            WHERE MATCH (p.first_nm, p.last_nm, p.full_nm, p.state, p.state_abbr, p.party_mem, p.party)
                  AGAINST (:parsed IN BOOLEAN MODE)
            ORDER BY 
                  p.current_mem DESC,
                  MATCH (p.first_nm, p.last_nm, p.full_nm, p.state, p.state_abbr, p.party_mem, p.party)
                        AGAINST (:parsed IN BOOLEAN MODE) DESC
            LIMIT :limit OFFSET :offset
            """;


        @SuppressWarnings("unchecked")
        List<Person> content = em.createNativeQuery(sql, Person.class)
                .setParameter("parsed", parsedQuery)
                .setParameter("limit", size)
                .setParameter("offset", offset)
                .getResultList();

        return new PageImpl<>(
                content,
                PageRequest.of(page, size),
                totalElements
        );
    }

}
