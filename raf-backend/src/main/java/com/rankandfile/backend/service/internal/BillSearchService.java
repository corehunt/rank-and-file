package com.rankandfile.backend.service.internal;

import com.rankandfile.backend.dto.BillDTO;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.mapper.BillMapper;
import com.rankandfile.backend.repository.BillSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillSearchService {

    private final BillSearchRepository billSearchRepository;
    private final BillMapper billMapper;

    public List<BillDTO> searchBills(String rawQuery, int page, int size) {
        String parsedQuery = buildBooleanQuery(rawQuery);
        if (parsedQuery.isEmpty()) {
            return List.of();
        }

        List<Bill> billEntities = billSearchRepository.searchBills(parsedQuery, page, size);

        return billEntities.stream()
                .map(billMapper::toBillDTO)
                .toList();
    }

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