package com.learning.security;

import com.learning.dbentity.identity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration.ms}")
    private int jwtExpirationMs;

    @Value("${app.jwt.temp-expiration-ms:300000}") // default 5 minutes
    private int jwtTempExpirationMs;

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @PostConstruct
    public void init() {
        logger.info("JWT Secret: {}", jwtSecret);
    }

    public String generateJwtToken(UserPrincipal userPrincipal,  Boolean... tem2fa){
        int jwtExpiryMs = tem2fa.length > 0 ? jwtTempExpirationMs : jwtExpirationMs;
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime()+jwtExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateJwtTokenFromUser(User user){
        return Jwts.builder()
                .setSubject(user.getUserName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime()+jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJwtToken(String authToken){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e){
            logger.error("Invalid jwt token: {}", e.getMessage());
        } catch (ExpiredJwtException e){
            logger.error("Jwt token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e){
            logger.error("Jwt token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e){
            logger.error("Jwt claims string is empty:  {}", e.getMessage());
        }
        return false;
    }
}
