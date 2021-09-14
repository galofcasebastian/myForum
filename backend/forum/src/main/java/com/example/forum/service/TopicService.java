package com.example.forum.service;

import com.example.forum.dto.TopicDto;
import com.example.forum.exceptions.domain.TopicAlreadyExists;

import java.util.List;

public interface TopicService {
    void save(TopicDto topicDto) throws TopicAlreadyExists;

    TopicDto getTopic(String topicName);

    List<TopicDto> getAllTopics();
}
