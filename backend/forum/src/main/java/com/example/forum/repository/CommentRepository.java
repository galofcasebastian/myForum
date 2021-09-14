package com.example.forum.repository;

import com.example.forum.model.Comment;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);

    List<Comment> findAllByUser(User user);

    Comment findCommentById(Long id);
}
