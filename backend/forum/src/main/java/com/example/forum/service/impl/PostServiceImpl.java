package com.example.forum.service.impl;

import com.example.forum.dto.PostRequest;
import com.example.forum.dto.PostResponse;
import com.example.forum.exceptions.domain.TopicNotFoundException;
import com.example.forum.exceptions.domain.ForumException;
import com.example.forum.mapper.PostMapper;
import com.example.forum.model.Post;
import com.example.forum.model.Topic;
import com.example.forum.model.User;
import com.example.forum.repository.PostRepository;
import com.example.forum.repository.TopicRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.PostService;
import com.example.forum.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class PostServiceImpl implements PostService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public PostServiceImpl(TopicRepository topicRepository, PostRepository postRepository, PostMapper postMapper,
                           UserService userService, UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void save(PostRequest postRequest) {
        if (postRequest.getPostName() == null || postRequest.getPostName().equals("")) {
            throw new ForumException("Please provide a name for the post");
        }
        if (postRequest.getTopicName() == null || postRequest.getTopicName().equals("")) {
            throw new ForumException("You need to select a topic for the post");
        }
        if (postRequest.getDescription() == null || postRequest.getDescription().equals("")) {
            throw new ForumException("Please provide a description for the post");
        }
        Topic topic = topicRepository.findTopicByTopicName(postRequest.getTopicName());
        Post post = postMapper.mapPostToDto(postRequest, topic, userService.getCurrentUser());
        topic.getPosts().add(post);
        postRepository.save(post);
    }

    @Override
    public PostResponse getPost(Long id) {
        Post post = postRepository.findPostById(id);
        return postMapper.mapDtoToPost(post);
    }

    @Override
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream().map(postMapper::mapDtoToPost).collect(toList());
    }

    @Override
    public List<PostResponse> getPostsByTopic(String topicName) {
        Topic topic = topicRepository.findTopicByTopicName(topicName);
        if (topic == null) {
            throw new TopicNotFoundException("Topic with name" + topicName + "does not exist");
        }
        List<Post> posts = postRepository.findAllByTopic(topic);
        return posts.stream().map(postMapper::mapDtoToPost).collect(toList());
    }

    @Override
    public List<PostResponse> getPostsByUser(String username) {
        User user = userRepository.findUserByUsername(username);
        return postRepository.findByUser(user).stream().map(postMapper::mapDtoToPost).collect(toList());
    }

    @Override
    public void deletePost(Long id) {
        User user = userService.getCurrentUser();
        //LOGGER.info(user.getUsername());
        //LOGGER.info(user.getRole());
        int ok = 0;
        if (user.getRole().equals("ROLE_ADMIN")) {
            postRepository.deleteById(id);
        } else if (user.getRole().equals("ROLE_USER")) {
            List<Post> posts = postRepository.findByUser(user);
            Post post = postRepository.findPostById(id);
            for (Post p : posts) {
                if (p.equals(post)) {
                    ok = 1;
                    break;
                }
            }
            if (ok == 1) {
                postRepository.deleteById(id);
            } else {
                throw new ForumException("You do not have permission to perform this action");
            }
        }
    }
}
