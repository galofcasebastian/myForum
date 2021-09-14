package com.example.forum.repository;

import com.example.forum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);

    User findUserByEmail(String email);

    User findUserById(Long id);

    User findUserByAccountActivationToken(String token);
}
