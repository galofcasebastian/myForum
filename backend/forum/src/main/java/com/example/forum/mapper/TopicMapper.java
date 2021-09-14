package com.example.forum.mapper;

import com.example.forum.dto.TopicDto;
import com.example.forum.model.Topic;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class TopicMapper {
    @Mapping(target = "numberOfPosts", expression = "java(getNumberOfPosts(topic))")
    public abstract TopicDto mapTopicToDto(Topic topic);

    public Integer getNumberOfPosts(Topic topic){
        return topic.getPosts().size();
    }

    @InheritInverseConfiguration
    @Mapping(target = "posts", ignore = true)
    public abstract Topic mapDtoToTopic(TopicDto topicDto);
}
