package com.upsc.ai.repository;

import com.upsc.ai.entity.Subject;
import com.upsc.ai.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findBySubjectAndNameIgnoreCase(Subject subject, String name);

    List<Topic> findBySubject(Subject subject);
}
