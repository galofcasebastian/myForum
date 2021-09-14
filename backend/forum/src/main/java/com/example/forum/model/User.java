package com.example.forum.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "email")
    private String email;

    @Column(name = "username")
    private String username;

    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "password")
    private String password;

    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "confirm_password")
    private String confirmPassword;

    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @JsonProperty(access = WRITE_ONLY)
    @Column(name = "account_activation_token")
    private String accountActivationToken;

    @Column(name="profile_image_url")
    private String profileImageUrl;

    @Column(name = "last_login_date")
    private String lastLoginDate;

    @Column(name = "last_login_date_display")
    private String lastLoginDateDisplay;

    @Column(name = "join_date")
    private String joinDate;

    @Column(name = "role")
    private String role; //ROLE_ADMIN, ROLE_USER

    @Column(name = "authorities")
    private String[] authorities;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "is_not_locked")
    private boolean isNotLocked;
}
