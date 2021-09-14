package com.example.forum.mapper;

import com.example.forum.dto.PostRequest;
import com.example.forum.dto.PostResponse;
import com.example.forum.model.Post;
import com.example.forum.model.Topic;
import com.example.forum.model.User;
import com.example.forum.repository.CommentRepository;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import org.apache.commons.lang3.RandomStringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PostMapper {
    @Autowired
    private CommentRepository commentRepository;

    @Mapping(target = "sendNotification", source = "postRequest.sendNotification")
    @Mapping(target = "createdDate", expression = "java(java.time.Instant.now())")
    @Mapping(target = "description", source = "postRequest.description")
    @Mapping(target = "topic", source = "topic")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", source = "postRequest.id")
    @Mapping(target = "url", expression = "java(createUrl())")
    public abstract Post mapPostToDto(PostRequest postRequest, Topic topic, User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "topicName", source = "topic.topicName")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "commentCount", expression = "java(commentCount(post))")
    @Mapping(target = "duration", expression = "java(getDuration(post))")
    public abstract PostResponse mapDtoToPost(Post post);

    public Integer commentCount(Post post) {
        return commentRepository.findByPost(post).size();
    }

    public String getDuration(Post post) {
        return TimeAgo.using(post.getCreatedDate().toEpochMilli());
    }

    public String createUrl() {
        return RandomStringUtils.randomAlphanumeric(128);
    }
}
