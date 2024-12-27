package com.rankandfile.backend.service.internal;

import com.rankandfile.backend.dto.BillDTO;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.mapper.BillMapper;
import com.rankandfile.backend.repository.BillSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillSearchService {

    private final BillSearchRepository billSearchRepository;
    private final BillMapper billMapper;

    /**
     * Searches bills in full-text boolean mode and returns a paged list of BillDTO.
     *
     * @param rawQuery the raw search string
     * @param page     zero-based page index
     * @param size     page size
     * @return a Page of BillDTO
     */
    public Page<BillDTO> searchBillsPaged(String rawQuery, int page, int size) {
        String parsedQuery = buildBooleanQuery(rawQuery);
        if (parsedQuery.isEmpty()) {
            // Return an empty page if there's no valid query
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Bill> billPage = billSearchRepository.searchBills(parsedQuery, page, size);

        return billPage.map(billMapper::toBillDTO);
    }

    /**
     * Builds a MySQL boolean-mode query for partial matching.
     * e.g. "tax credit" -> "+tax* +credit*"
     */
    private String buildBooleanQuery(String raw) {
        if (raw == null || raw.isBlank()) return "";

        String[] tokens = raw.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (!token.isBlank()) {
                sb.append("+").append(token).append("* ");
            }
        }
        return sb.toString().trim();
    }
}