package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Congress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CongressRepository extends JpaRepository<Congress, Integer> {
    Optional<Congress> findByCongressNumber(Integer congressNumber);
}