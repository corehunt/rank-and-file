package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BillRepository extends JpaRepository<Bill, String> {

    List<Bill> findByCongressInAndBillNoIn(Set<Integer> congressNumbers, Set<Integer> billNumbers);

    Bill findByCongressAndBillNoAndBillType(String congress, String billNo, String billType);

    List<Bill> findTop10ByOrderByIntroducedDtDesc();

    List<Bill> findByCreateTimestampAfter(LocalDateTime since);
}
