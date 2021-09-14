package com.example.forum.exceptions;

import static org.springframework.http.HttpStatus.*;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.forum.exceptions.domain.*;
import com.example.forum.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Objects;

@RestControllerAdvice
public class ExceptionHandling implements ErrorController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact the administrator for more information";
    private static final String ACCOUNT_NOT_ACTIVATED = "Your account is not activated. Please activate it by using the link sent via email";
    private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
    private static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
    private static final String INCORRECT_CREDENTIALS = "Username / password incorrect, please try again";
    private static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
    private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permissions to perform this task";
    private static final String TOPIC_ALREADY_EXISTS = "Topic already exists";
    private static final String PASSWORDS_DO_NOT_MATCH = "The password does not match";
    private static final String INCORRECT_PASSWORD = "Incorrect password";
    private static final String NOT_AN_IMAGE_FILE = "This type of file is not supported, please select an image file";

    @ExceptionHandler(NotAnImageFileException.class)
    public ResponseEntity<HttpResponse> notAnImageFileException() {
        return createHttpResponse(BAD_REQUEST, NOT_AN_IMAGE_FILE);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException() {
        return createHttpResponse(BAD_REQUEST, ACCOUNT_NOT_ACTIVATED);
    }

    @ExceptionHandler(PasswordNotMatchingException.class)
    public ResponseEntity<HttpResponse> passwordNotMatchingException() {
        return createHttpResponse(BAD_REQUEST, PASSWORDS_DO_NOT_MATCH);
    }

    @ExceptionHandler(IncorrectPassword.class)
    public ResponseEntity<HttpResponse> incorrectPasswordException() {
        return createHttpResponse(BAD_REQUEST, INCORRECT_PASSWORD);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException() {
        return createHttpResponse(BAD_REQUEST, INCORRECT_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException() {
        return createHttpResponse(FORBIDDEN, NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException() {
        return createHttpResponse(UNAUTHORIZED, ACCOUNT_LOCKED);
    }

    @ExceptionHandler(ForumException.class)
    public ResponseEntity<HttpResponse> blogException(ForumException exception){
        return createHttpResponse(FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception) {
        return createHttpResponse(UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(EmailExistException.class)
    public ResponseEntity<HttpResponse> emailExistException(EmailExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UsernameExistException.class)
    public ResponseEntity<HttpResponse> usernameExistException(UsernameExistException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(TopicAlreadyExists.class)
    public ResponseEntity<HttpResponse> topicAlreadyExists(TopicAlreadyExists exception) {
        return createHttpResponse(BAD_REQUEST, TOPIC_ALREADY_EXISTS);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception) {
        return createHttpResponse(BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(NoHandlerFoundException exception) {
        return createHttpResponse(BAD_REQUEST, "There is no mapping for this URL");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
        return createHttpResponse(METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> iOException(IOException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE);
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus,
                httpStatus.getReasonPhrase(), message), httpStatus);
    }
}
