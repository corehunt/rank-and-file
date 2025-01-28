package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.domain.StateDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateRepository extends JpaRepository<StateDomain, Integer> {

    Optional<StateDomain> findByStateNm(String state);
}
