package com.rankandfile.backend.repository;

import com.rankandfile.backend.entity.Leadership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LeadershipRepository extends JpaRepository<Leadership, Long> {

    @Query("SELECT l from Leadership l WHERE l.currentLeader = 'true'")
    List<Leadership> findByCurrentLeader();
}
