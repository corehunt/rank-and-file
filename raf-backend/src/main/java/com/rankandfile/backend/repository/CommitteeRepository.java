package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitteeRepository extends JpaRepository<Committee, String> {
    Committee findBySysCode(String sysCode);
}

