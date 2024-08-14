package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRepository extends JpaRepository<Action, String> {
}
