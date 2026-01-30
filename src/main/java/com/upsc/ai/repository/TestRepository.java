package com.upsc.ai.repository;

import com.upsc.ai.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    @EntityGraph(attributePaths = { "questions", "subject", "topic" })
    List<Test> findAll();

    @EntityGraph(attributePaths = { "questions", "subject", "topic" })
    Optional<Test> findById(Long id);

    @EntityGraph(attributePaths = { "subject", "topic", "createdBy" })
    List<Test> findByCreatedBy_Id(Long userId);
}
