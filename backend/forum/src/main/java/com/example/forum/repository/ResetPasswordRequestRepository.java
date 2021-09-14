package com.example.forum.repository;

import com.example.forum.model.ResetPasswordRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetPasswordRequestRepository extends JpaRepository<ResetPasswordRequest, Long> {
    ResetPasswordRequest findByToken(String token);
}
