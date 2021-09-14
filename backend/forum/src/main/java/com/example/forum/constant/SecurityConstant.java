package com.example.forum.constant;

import java.util.concurrent.TimeUnit;

public class SecurityConstant {
    public static final long EXPIRATION_TIME = TimeUnit.DAYS.toMillis(7); // 7 days
    public static final long PASSWORD_RESET_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(1); // 1 hour
    public static final long ACCOUNT_VERIFICATION_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(1);
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String SEBI_PROD_SRL = "Sebi Prod, SRL";
    public static final String SEBI_PROD_ADMINISTRATION = "myForum";
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to login in order to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permissions to use this service";
    public static final String OPTIONS_HTTP_METHOD = "Options";
    public static final String[] PUBLIC_URLS = {"/user/login", "/user/register", "/user/reset-password-request", "/user/reset-password/**", "/user/image/**", "/user/account-verification/**",
            "/comment/by-post/**", "/post/all", "/topic/all", "/post/by-topic/**", "/post/**", "/user/userinho"};
}
