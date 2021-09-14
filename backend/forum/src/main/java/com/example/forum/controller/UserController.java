package com.example.forum.controller;

import static com.example.forum.constant.FileConstant.*;
import static com.example.forum.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import com.example.forum.dto.ChangePasswordRequest;
import com.example.forum.dto.ResetPasswordModel;
import com.example.forum.exceptions.domain.*;
import com.example.forum.model.HttpResponse;
import com.example.forum.model.User;
import com.example.forum.model.UserPrincipal;
import com.example.forum.exceptions.ExceptionHandling;
import com.example.forum.service.UserService;
import com.example.forum.utility.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping(value = {"/user"})
public class UserController extends ExceptionHandling {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String EMAIL_SENT = "An email with reset password instructions has been sent to: ";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
    public static final String PASSWORD_UPDATED_SUCCESSFULLY = "Password updated successfully";
    public static final String ACCOUNT_ACTIVATED_SUCCESSFULLY = "Account activated successfully, you can login now";
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException, PasswordNotMatchingException {
        User newUser = userService.register(user.getEmail(), user.getUsername(), user.getPassword(), user.getConfirmPassword());
        return new ResponseEntity<>(newUser, OK);
    }

    @GetMapping("/account-verification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable("token") String token) {
        userService.activateAccount(token);
        return new ResponseEntity<>(ACCOUNT_ACTIVATED_SUCCESSFULLY, OK);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authenticate(user.getUsername(), user.getPassword());
        User loginUser = userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, OK);
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User newUser = userService.addNewUser(username, email, role, Boolean.parseBoolean(isNonLocked),
                Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(newUser, OK);
    }

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(@RequestParam("currentUsername") String currentUsername,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
        User updatedUser = userService.updateUser(currentUsername, username, email, role, Boolean.parseBoolean(isNonLocked),
                Boolean.parseBoolean(isActive), profileImage);
        return new ResponseEntity<>(updatedUser, OK);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> findUser(@PathVariable("username") String username) {
        User user = userService.findUserByUsername(username);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getUsers();
        return new ResponseEntity<>(users, OK);
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<HttpResponse> resetPasswordRequest(@RequestBody String email) throws EmailNotFoundException, MessagingException {
        userService.resetPasswordRequest(email);
        return response(OK, EMAIL_SENT + email);
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("token") String token, @RequestBody ResetPasswordModel resetPasswordModel) throws PasswordNotMatchingException {
        userService.resetPassword(resetPasswordModel);
        return response(OK, PASSWORD_UPDATED_SUCCESSFULLY);
    }

    @PostMapping("/change-password")
    public ResponseEntity<HttpResponse> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) throws PasswordNotMatchingException, IncorrectPassword {
        userService.changePassword(changePasswordRequest);
        return response(OK, PASSWORD_UPDATED_SUCCESSFULLY);
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("username") String username) throws IOException {
        userService.deleteUser(username);
        return response(NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username,
                                                   @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException, EmailExistException, IOException, UsernameExistException, NotAnImageFileException {
        User user = userService.updateProfileImage(username, profileImage);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping(path = "/image/{username}/{filename}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + filename));
    }

    @GetMapping(path = "/image/profile/{username}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int bytesRead;
            byte[] chunk = new byte[1024];
            while ((bytesRead = inputStream.read(chunk)) > 0) {
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    @GetMapping("/userinho")
    public ResponseEntity<String> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return new ResponseEntity<>(username, OK);
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        HttpResponse body = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase(), message);
        return new ResponseEntity<>(body, httpStatus);
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }
}
