package com.rankandfile.backend.service.internal;

import com.rankandfile.backend.dto.BillDTO;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.mapper.BillMapper;
import com.rankandfile.backend.repository.BillRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BillService {

    private final BillRepository billRepository;
    private final BillMapper billMapper;

    public BillService(BillRepository billRepository, BillMapper billMapper) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
    }

    public BillDTO getBillById(String billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with given id: " + billId));
        log.info("Bill returned is {}", bill);

        return billMapper.toBillDTO(bill);
    }

}
