package com.example.forum.service;

import com.example.forum.dto.ChangePasswordRequest;
import com.example.forum.dto.ResetPasswordModel;
import com.example.forum.exceptions.domain.*;
import com.example.forum.model.User;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {
    User register(String email, String username, String password, String confirmPassword) throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException, PasswordNotMatchingException;

    void activateAccount(String token);

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    User addNewUser(String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException;// NotAnImageFileException;

    User updateUser(String currentUsername, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException;//, NotAnImageFileException;

    void deleteUser(String username) throws IOException;

    void resetPasswordRequest(String email) throws MessagingException, EmailNotFoundException;

    void resetPassword(ResetPasswordModel resetPasswordModel) throws PasswordNotMatchingException;

    void changePassword(ChangePasswordRequest changePasswordRequest) throws IncorrectPassword, PasswordNotMatchingException;

    User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException;//, NotAnImageFileException;

    User getCurrentUser();
}
