package com.upsc.ai.repository;

import com.upsc.ai.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByCreatedBy_Id(Long userId);
}
