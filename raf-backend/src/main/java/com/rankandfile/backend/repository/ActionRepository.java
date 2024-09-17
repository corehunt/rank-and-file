package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, String> {

    List<Action> findByBillBillId(String billId);
}
