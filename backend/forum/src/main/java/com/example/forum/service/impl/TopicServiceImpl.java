package com.example.forum.service.impl;

import com.example.forum.dto.TopicDto;
import com.example.forum.exceptions.domain.ForumException;
import com.example.forum.exceptions.domain.TopicAlreadyExists;
import com.example.forum.exceptions.domain.TopicNotFoundException;
import com.example.forum.mapper.TopicMapper;
import com.example.forum.model.Topic;
import com.example.forum.repository.TopicRepository;
import com.example.forum.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class TopicServiceImpl implements TopicService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    @Autowired
    public TopicServiceImpl(TopicRepository topicRepository, TopicMapper topicMapper) {
        this.topicRepository = topicRepository;
        this.topicMapper = topicMapper;
    }

    @Override
    public void save(TopicDto topicDto) throws TopicAlreadyExists {
        if (topicRepository.existsByTopicName(topicDto.getTopicName())) {
            throw new TopicAlreadyExists("Topic with name '" + topicDto.getTopicName() + "' already exists");
        } else if (topicDto.getTopicName() == null || topicDto.getTopicName().equals("")) {
            throw new ForumException("Please provide a name for the topic");
        } else if (topicDto.getDescription() == null || topicDto.getDescription().equals("")) {
            throw new ForumException("Please provide a description for the topic");
        } else {
            Topic topic = topicRepository.save(topicMapper.mapDtoToTopic(topicDto));
            topicDto.setId(topic.getId());
        }
    }

    @Override
    public TopicDto getTopic(String topicName) {
        Topic topic = topicRepository.findTopicByTopicName(topicName);
        if (topic == null) {
            throw new TopicNotFoundException("Topic with name " + topicName + " does not exist");
        }
        //LOGGER.info(topic.getPosts().toString());
        return topicMapper.mapTopicToDto(topic);
    }

    @Override
    public List<TopicDto> getAllTopics() {
        return topicRepository.findAll().stream().map(topicMapper::mapTopicToDto).collect(toList());
    }
}
