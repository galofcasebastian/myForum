package com.example.forum.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Topic {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "description")
    private String description;

    @Column(name = "created_date")
    private Instant createdDate;

    @OneToMany(fetch = LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private List<Post> posts;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
