package com.example.forum.repository;

import com.example.forum.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Topic findTopicByTopicName(String topicName);

    boolean existsByTopicName(String topicName);
}
