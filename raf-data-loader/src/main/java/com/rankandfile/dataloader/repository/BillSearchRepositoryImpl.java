package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.Bill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BillSearchRepositoryImpl implements BillSearchRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Bill> searchBills(String parsedQuery, int page, int size) {
        int offset = page * size;

        // 1) Count query
        String countSql = """
            SELECT COUNT(*)
            FROM RAF_BILL b
            WHERE MATCH (b.BILL_TITLE, b.SUMMARY_TXT, b.BILL_NO, b.BILL_TYPE, b.ORIGIN_CHAMBER,
                         b.POLICY_AREA, b.LEGISLATIVE_SUBJECTS, b.SPONSORS_TXT)
                  AGAINST (:parsed IN BOOLEAN MODE)
            """;

        Number countResult = (Number) em.createNativeQuery(countSql)
                .setParameter("parsed", parsedQuery)
                .getSingleResult();
        long totalElements = countResult.longValue();

        // 2) Main query for content
        String sql = """
            SELECT b.*
            FROM RAF_BILL b
            WHERE MATCH (b.BILL_TITLE, b.SUMMARY_TXT, b.BILL_NO, b.BILL_TYPE, b.ORIGIN_CHAMBER,
                         b.POLICY_AREA, b.LEGISLATIVE_SUBJECTS, b.SPONSORS_TXT)
                  AGAINST (:parsed IN BOOLEAN MODE)
            ORDER BY MATCH (b.BILL_TITLE, b.SUMMARY_TXT, b.BILL_NO, b.BILL_TYPE, b.ORIGIN_CHAMBER,
                            b.POLICY_AREA, b.LEGISLATIVE_SUBJECTS, b.SPONSORS_TXT)
                     AGAINST (:parsed IN BOOLEAN MODE) DESC,
                     b.INTRODUCED_DT DESC
            LIMIT :limit OFFSET :offset
            """;

        @SuppressWarnings("unchecked")
        List<Bill> content = em.createNativeQuery(sql, Bill.class)
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
