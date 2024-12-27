package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Bill;
import org.springframework.data.domain.Page;

public interface BillSearchRepository {
    /**
     * Searches Bill entities via MySQL full-text boolean mode,
     * returning them sorted by text relevance desc, then introduced date desc.
     *
     * @param parsedQuery Boolean-mode query string (e.g. "+tax* +credit*")
     * @param page        zero-based page index
     * @param size        number of results per page
     * @return a Page of Bill entities matching the search
     */
    Page<Bill> searchBills(String parsedQuery, int page, int size);
}