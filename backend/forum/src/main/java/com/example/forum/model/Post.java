package com.example.forum.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "post_name")
    private String postName;

    @Column(name = "url")
    private String url;

    @Column(name = "description")
    private String description;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "send_notification")
    private boolean sendNotification;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "topic_id", referencedColumnName = "id")
    private Topic topic;
}
