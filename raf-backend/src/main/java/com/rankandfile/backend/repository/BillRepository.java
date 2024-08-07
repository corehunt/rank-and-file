package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BillRepository extends JpaRepository<Bill, String> {

    @Query("SELECT b FROM Bill b WHERE b.congress = :congressNo AND b.billNo = :billNo")
    Bill findByCongressAndBillNo(Integer congressNo, Integer billNo);

}
