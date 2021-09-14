package com.example.forum.repository;

import com.example.forum.model.Post;
import com.example.forum.model.Topic;
import com.example.forum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByTopic(Topic topic);

    List<Post> findByUser(User user);

    Post findPostById(Long id);
}
