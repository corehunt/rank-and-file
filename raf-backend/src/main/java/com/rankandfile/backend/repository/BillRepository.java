package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface BillRepository extends JpaRepository<Bill, String> {

    List<Bill> findByCongressInAndBillNoIn(Set<Integer> congressNumbers, Set<Integer> billNumbers);

    Bill findByCongressAndBillNoAndBillType(Integer congress, Integer billNo, String billType);
}
