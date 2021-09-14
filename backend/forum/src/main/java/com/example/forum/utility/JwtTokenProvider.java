package com.example.forum.utility;

import com.auth0.jwt.JWT;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.example.forum.constant.SecurityConstant.*;
import static java.util.Arrays.stream;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.forum.model.User;
import com.example.forum.model.UserPrincipal;
import com.example.forum.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;
    private final UserRepository userRepository;

    @Autowired
    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateJwtToken(UserPrincipal userPrincipal) {
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create().withIssuer(SEBI_PROD_SRL).withAudience(SEBI_PROD_ADMINISTRATION)
                .withIssuedAt(new Date()).withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, claims).withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    public String generateJwtTokenForPasswordReset(Long userId) {
        User user = userRepository.findUserById(userId);
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create().withIssuer(SEBI_PROD_SRL).withAudience(SEBI_PROD_ADMINISTRATION)
                .withIssuedAt(new Date()).withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, claims).withExpiresAt(new Date(System.currentTimeMillis() + PASSWORD_RESET_EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    public String generateJwtTokenForAccountActivation(String username) {
        User user = userRepository.findUserByUsername(username);
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create().withIssuer(SEBI_PROD_SRL).withAudience(SEBI_PROD_ADMINISTRATION)
                .withIssuedAt(new Date()).withSubject(userPrincipal.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCOUNT_VERIFICATION_EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }

    public boolean isTokenValid(String username, String token) {
        JWTVerifier verifier = getJwtVerifier();
        return StringUtils.isNotEmpty(username) && !isTokenExpired(verifier, token);
    }

    public boolean isResetPasswordTokenValid(String token) {
        JWTVerifier verifier = getJwtVerifier();
        return !isTokenExpired(verifier, token);
    }

    public String getSubject(String token) {
        JWTVerifier verifier = getJwtVerifier();
        return verifier.verify(token).getSubject();
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expirationDate = verifier.verify(token).getExpiresAt();
        return expirationDate.before(new Date());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJwtVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJwtVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(SEBI_PROD_SRL).build();
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : userPrincipal.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }
        return authorities.toArray(new String[0]);
    }
}
