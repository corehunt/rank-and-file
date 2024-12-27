package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Bill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BillSearchRepositoryImpl implements BillSearchRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Bill> searchBills(String parsedQuery, int page, int size) {
        int offset = page * size;

        String sql = """
          SELECT b.*
          FROM RAF_BILL b
          WHERE MATCH (b.BILL_TITLE, b.SUMMARY_TXT, b.BILL_NO, b.ORIGIN_CHAMBER,
                       b.POLICY_AREA, b.LEGISLATIVE_SUBJECTS, b.SPONSORS_TXT)
                AGAINST (:parsed IN BOOLEAN MODE)
          ORDER BY MATCH (b.BILL_TITLE, b.SUMMARY_TXT, b.BILL_NO, b.ORIGIN_CHAMBER,
                       b.POLICY_AREA, b.LEGISLATIVE_SUBJECTS, b.SPONSORS_TXT)
                AGAINST (:parsed IN BOOLEAN MODE) DESC,
                b.INTRODUCED_DT DESC
          LIMIT :limit OFFSET :offset
          """;


        @SuppressWarnings("unchecked")
        List<Bill> results = em.createNativeQuery(sql, Bill.class)
                .setParameter("parsed", parsedQuery)
                .setParameter("limit", size)
                .setParameter("offset", offset)
                .getResultList();

        return results;
    }
}
