package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.domain.StateDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateRepository extends JpaRepository<StateDomain, Integer> {

    Optional<StateDomain> findByStateNm(String state);
}
