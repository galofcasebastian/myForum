package com.example.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    private Long id;
    private String topicName;
    private String postName;
    private String url;
    private String description;
    private boolean sendNotification;
}
