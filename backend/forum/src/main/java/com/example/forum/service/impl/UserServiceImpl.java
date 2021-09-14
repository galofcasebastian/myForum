package com.example.forum.service.impl;

import static com.example.forum.constant.FileConstant.*;
import static com.example.forum.constant.UserImplConstant.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static com.example.forum.enumeration.Role.ROLE_USER;

import static org.springframework.http.MediaType.*;

import com.example.forum.dto.ChangePasswordRequest;
import com.example.forum.dto.ResetPasswordModel;
import com.example.forum.exceptions.domain.*;
import com.example.forum.model.*;
import com.example.forum.enumeration.Role;
import com.example.forum.repository.ResetPasswordRequestRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.service.*;
import com.example.forum.utility.JwtTokenProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final MailService mailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResetPasswordRequestRepository resetPasswordRequestRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, LoginAttemptService loginAttemptService,
                           MailService mailService, JwtTokenProvider jwtTokenProvider, ResetPasswordRequestRepository resetPasswordRequestRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.mailService = mailService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.resetPasswordRequestRepository = resetPasswordRequestRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            LOGGER.error(NO_USER_FOUND_WITH_USERNAME + username);
            throw new UsernameNotFoundException(NO_USER_FOUND_WITH_USERNAME + username);
        } else {
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
            String lastLogin = dateFormat.format(date);
            user.setLastLoginDate(lastLogin);
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("Returning found user by username: " + username);
            return userPrincipal;
        }
    }

    @Override
    public void activateAccount(String token) {
        User user = userRepository.findUserByAccountActivationToken(token);
        if (user == null) {
            throw new ForumException("Invalid token");
        }
        enableUser(user);
    }

    @Override
    public User register(String email, String username, String password, String confirmPassword) throws UserNotFoundException,
            EmailExistException, UsernameExistException, PasswordNotMatchingException {
        if (!password.equals(confirmPassword)) {
            throw new PasswordNotMatchingException("Password does not match");
        }
        validateNewUsernameAndEmail(EMPTY, username, email);
        User user = new User();
        user.setUserId(generateUserId());
        String encodedPassword = encodePassword(password);
        user.setUsername(username);
        user.setEmail(email);
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
        String joinDate = dateFormat.format(date);
        user.setJoinDate(joinDate);
        user.setPassword(encodedPassword);
        user.setActive(false);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        String token = jwtTokenProvider.generateJwtTokenForAccountActivation(username);
        user.setAccountActivationToken(token);
        String ACTIVATE_ACCOUNT_URL = "http://localhost:4200/account-verification/";
        mailService.sendMail(new NotificationEmail("Activate your account", user.getEmail(), "Hello " + user.getUsername() + "\n\nPlease click on the next link in order to activate your account: "
                + ACTIVATE_ACCOUNT_URL + token + "\n\nWe wish you all the best, \nSebi Prod, SRL support team"));
        userRepository.save(user);

        return user;
    }

    @Override
    public User addNewUser(String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException { //, NotAnImageFileException {
        validateNewUsernameAndEmail(EMPTY, username, email);
        User user = new User();
        String password = generatePassword();
        user.setUserId(generateUserId());
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
        String joinDate = dateFormat.format(date);
        user.setJoinDate(joinDate);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        saveProfileImage(user, profileImage);
        mailService.sendMail(new NotificationEmail("Your new account", user.getEmail(), "Hello" + user.getUsername() + "\n\nThis is your new password for your account: "
                + password + "\nYou can change it anytime you want\n\nWe wish you all the best, \nSebi Prod, SRL support team"));
        return user;
    }

    @Override
    public User updateUser(String currentUsername, String newUsername, String newEmail, String role,
                           boolean isNonLocked, boolean isActive, MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        userRepository.save(currentUser);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }

    @Override
    public void deleteUser(String username) throws IOException {
        User user = userRepository.findUserByUsername(username);
        Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(new File(userFolder.toString()));
        userRepository.deleteById(user.getId());
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {//, NotAnImageFileException {
        User user = validateNewUsernameAndEmail(username, null, null);
        saveProfileImage(user, profileImage);
        return user;
    }

    @Override
    public void resetPasswordRequest(String email) throws EmailNotFoundException {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        }

        String token = jwtTokenProvider.generateJwtTokenForPasswordReset(user.getId());

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken(token);
        resetPasswordRequest.setUser(user);
        resetPasswordRequestRepository.save(resetPasswordRequest);

        String PASSWORD_RESET_URL = "http://localhost:4200/reset-password/";

        mailService.sendMail(new NotificationEmail("Reset your password", user.getEmail(), "Hello " + user.getUsername() +
                ", \n\nPlease click on the next link in order to change your password: " + PASSWORD_RESET_URL + token +
                "\n \nWe wish you all the best, \nSebi Prod, SRL support team"));

    }

    @Override
    public void resetPassword(ResetPasswordModel resetPasswordModel) throws PasswordNotMatchingException {
        if (!jwtTokenProvider.isResetPasswordTokenValid(resetPasswordModel.getToken())) {
            throw new ForumException("Invalid token");
        }

        ResetPasswordRequest resetPasswordRequest = resetPasswordRequestRepository.findByToken(resetPasswordModel.getToken());
        if (resetPasswordRequest == null) {
            throw new ForumException("Token does not exist");
        }

        if (!resetPasswordModel.getPassword().equals(resetPasswordModel.getConfirmPassword())) {
            throw new PasswordNotMatchingException("Password does not match");
        }

        String encodedPassword = encodePassword(resetPasswordModel.getPassword());

        User user = resetPasswordRequest.getUser();
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);

        resetPasswordRequestRepository.delete(resetPasswordRequest);
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) throws IncorrectPassword, PasswordNotMatchingException {
        User currentUser = getCurrentUser();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())) {
            throw new IncorrectPassword("Incorrect password");
        }
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            throw new PasswordNotMatchingException("Password does not match");
        }
        if (changePasswordRequest.getCurrentPassword().equals(changePasswordRequest.getNewPassword())) {
            throw new ForumException("The new password cannot be the same as the old one");
        }
        currentUser.setPassword(encodePassword(changePasswordRequest.getNewPassword()));
        userRepository.save(currentUser);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (authentication instanceof AnonymousAuthenticationToken) {
            LOGGER.info("WTF");
        }
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            LOGGER.info("YES");
        }
        LOGGER.info(String.valueOf(principal));
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof Principal) {
            username = ((Principal) principal).getName();
        } else if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else {
            username = String.valueOf(principal);
        }
        LOGGER.info(username);
        LOGGER.info(String.valueOf(this.isAuthenticated()));
        return userRepository.findUserByUsername(username);
    }

    private void enableUser(User user) {
        user.setActive(true);
        user.setAccountActivationToken(null);
        userRepository.save(user);
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException, NotAnImageFileException {
        if (profileImage != null) {
            if (!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
                throw new NotAnImageFileException(profileImage.getOriginalFilename() + NOT_AN_IMAGE_FILE);
            }
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
            if (!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                LOGGER.info(DIRECTORY_CREATED + userFolder);
            }
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
            userRepository.save(user);
            LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH +
                username + DOT + JPG_EXTENSION).toUriString();
    }

    private void validateLoginAttempt(User user) {
        if (user.isNotLocked()) {
            if (loginAttemptService.exceededMaxAttempts(user.getUsername())) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAscii(12);
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    private String getTemporaryProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
            throws UserNotFoundException, UsernameExistException, EmailExistException {
        User userByNewUsername = findUserByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);
        if (StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findUserByUsername(currentUsername);
            if (currentUsername == null) {
                throw new UserNotFoundException(NO_USER_FOUND_WITH_USERNAME + currentUsername);
            }
            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistException(USERNAME_ALREADY_TAKEN);
            }
            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if (userByNewUsername != null) {
                throw new UsernameExistException(USERNAME_ALREADY_TAKEN);
            }
            if (userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }
}
