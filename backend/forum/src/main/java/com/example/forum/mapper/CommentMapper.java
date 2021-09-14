package com.example.forum.mapper;

import com.example.forum.dto.CommentDto;
import com.example.forum.model.Comment;
import com.example.forum.model.Post;
import com.example.forum.model.User;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "commentDto.text")
    @Mapping(target = "createdDate", expression = "java(java.time.Instant.now())")
    @Mapping(target = "post", source = "post")
    @Mapping(target = "user", source = "user")
    public abstract Comment mapDtoToComment(CommentDto commentDto, Post post, User user);

    @Mapping(target = "postId", expression = "java(comment.getPost().getId())")
    @Mapping(target = "username", expression = "java(comment.getUser().getUsername())")
    @Mapping(target = "createdDate", expression = "java(getDuration(comment))")
    public abstract CommentDto mapCommentToDto(Comment comment);

    public String getDuration(Comment comment) {
        return TimeAgo.using(comment.getCreatedDate().toEpochMilli());
    }
}
