package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {
}