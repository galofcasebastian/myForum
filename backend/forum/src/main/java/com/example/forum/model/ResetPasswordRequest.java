package com.example.forum.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Data
public class ResetPasswordRequest {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @JsonProperty(access = WRITE_ONLY)
    @Column(nullable = false, updatable = false)
    private Long id;

    private String token;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
