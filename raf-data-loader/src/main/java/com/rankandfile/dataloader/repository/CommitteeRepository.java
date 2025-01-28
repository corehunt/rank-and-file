package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitteeRepository extends JpaRepository<Committee, String> {
    Committee findBySysCode(String sysCode);
}

