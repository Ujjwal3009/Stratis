package com.upsc.ai.repository;

import com.upsc.ai.entity.UserTestMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTestMetricsRepository extends JpaRepository<UserTestMetrics, Long> {
    Optional<UserTestMetrics> findByTestAttemptId(Long testAttemptId);
}
