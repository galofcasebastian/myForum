package com.example.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopicDto {
    private Long id;
    private String topicName;
    private String description;
    private Integer numberOfPosts;
}
