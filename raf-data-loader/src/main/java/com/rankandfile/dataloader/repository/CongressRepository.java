package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.Congress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CongressRepository extends JpaRepository<Congress, Integer> {
    Optional<Congress> findByCongressNumber(Integer congressNumber);
}