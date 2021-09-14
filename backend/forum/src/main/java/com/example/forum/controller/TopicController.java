package com.example.forum.controller;

import com.example.forum.dto.TopicDto;
import com.example.forum.exceptions.ExceptionHandling;
import com.example.forum.exceptions.domain.TopicAlreadyExists;
import com.example.forum.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = "/topic")
public class TopicController extends ExceptionHandling {
    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping("/create/")
    public ResponseEntity<TopicDto> createTopic(@RequestBody TopicDto topicDto) throws TopicAlreadyExists {
        topicService.save(topicDto);
        return new ResponseEntity<>(topicDto, CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TopicDto>> getAllTopics(){
        List<TopicDto> topics = topicService.getAllTopics();
        return new ResponseEntity<>(topics, OK);
    }

    @GetMapping("/{topicName}")
    public ResponseEntity<TopicDto> getTopic(@PathVariable("topicName") String topicName){
        TopicDto topicDto = topicService.getTopic(topicName);
        return new ResponseEntity<>(topicDto, OK);
    }
}
