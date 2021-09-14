package com.example.forum.service;

import com.example.forum.dto.PostRequest;
import com.example.forum.dto.PostResponse;

import java.util.List;

public interface PostService {
    void save(PostRequest postRequest);

    PostResponse getPost(Long id);

    List<PostResponse> getAllPosts();

    List<PostResponse> getPostsByTopic(String topicName);

    List<PostResponse> getPostsByUser(String username);

    void deletePost(Long id);
}
