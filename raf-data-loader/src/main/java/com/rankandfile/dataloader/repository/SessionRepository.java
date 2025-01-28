package com.rankandfile.dataloader.repository;

import com.rankandfile.dataloader.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {
}