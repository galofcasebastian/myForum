package com.example.forum.exceptions.domain;

public class TopicAlreadyExists extends Exception {
    public TopicAlreadyExists(String message) {
        super(message);
    }
}
