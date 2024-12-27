package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Bill;

import java.util.List;

public interface BillSearchRepository {
    /**
     * Searches Bill entities via MySQL full-text boolean mode,
     * returning them sorted by text relevance desc, then introduced date desc.
     *
     * @param parsedQuery Boolean-mode query string (e.g. "+tax* +credit*")
     * @param page  Zero-based page index
     * @param size  Number of results per page
     * @return List of Bill entities matching the search
     */
    List<Bill> searchBills(String parsedQuery, int page, int size);
}