package com.ndungutse.auth_service.security;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.jwt.expirationMs}")
    private String jwtExpirationMS;

    // Get Jwt from header of the request
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        logger.info("Authorization Header: { }", bearerToken);
        if (bearerToken == null) {
            return null;
        }

        String token = bearerToken.substring(7);

        logger.info("Token: { }", token);

        return token;
    }

    // Generate Jwt token
    public String generateJwtTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder().subject(username).issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + Long.parseLong(jwtExpirationMS)))
                .signWith(key()).compact();

    }

    // Verify and Decode and extract payload from Jwt token
    public String getUserNameFromJwtToken(String token) {
        try {
            String username = Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return username;

            // Tampered JWT
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new RuntimeException("Invalid JWT signature");

            // Invalid JWT
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Invalid JWT token");

            // Expired JWT
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Expired JWT token");
        }

    }

    // Key encription
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Validate Jwt token
    public boolean validateJwtToken(String authToken) {

        try {
            System.out.println("Validating token");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (UnsupportedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            return false;
        }

    }
}
