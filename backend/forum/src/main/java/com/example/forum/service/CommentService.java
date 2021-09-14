package com.example.forum.service;

import com.example.forum.dto.CommentDto;

import java.util.List;

public interface CommentService {
    void save(CommentDto commentDto);

    List<CommentDto> getAllCommentsForPost(Long postId);

    List<CommentDto> getAllCommentsForUser(String username);

    void deleteComment(Long id);
}
