package com.upsc.ai.repository;

import com.upsc.ai.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    List<TestAttempt> findByUser_Id(Long userId);

    List<TestAttempt> findByUserOrderByStartedAtDesc(com.upsc.ai.entity.User user);
}
