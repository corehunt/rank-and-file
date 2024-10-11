package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Text;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextRepository extends JpaRepository<Text, String> {

    List<Text> findByBillBillId(String billId);
}
