package com.rankandfile.backend.service.internal;

import com.rankandfile.backend.dto.BillDTO;
import com.rankandfile.backend.entity.Bill;
import com.rankandfile.backend.mapper.BillMapper;
import com.rankandfile.backend.repository.BillRepository;
import com.rankandfile.backend.util.IdGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BillService {

    private final BillRepository billRepository;
    private final BillMapper billMapper;
    private final IdGenerator idGenerator;

    public BillService(BillRepository billRepository, BillMapper billMapper, IdGenerator idGenerator) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
        this.idGenerator = idGenerator;
    }

    public BillDTO getBillByCongressTypeNumber(String congressNo, String billType, String billNo) {
        String billId = idGenerator.generateBillId(congressNo, billType, billNo);
        log.info("BillId is {}", billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with given id: " + billId));
        log.info("Bill returned is {}", bill);

        return billMapper.toBillDTO(bill);
    }

}
