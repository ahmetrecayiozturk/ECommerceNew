package com.ecommerce.userservice.infrastructure.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {


    private final String my_secret_key = "thisismysecretkeyanditshouldbeverylonganditshouldnotbehardcoded";

    private final long myExpirationTime = 3600000; // 1 hour in milliseconds

    public String generateToken(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + myExpirationTime))
                .signWith(SignatureAlgorithm.HS256, my_secret_key)
                .compact();
    }
    public String extractEmail(String token){
        return Jwts.parser()
                .setSigningKey(my_secret_key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token){
        return Jwts.parser()
                .setSigningKey(my_secret_key)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    public String getTokenFromHeader(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " kısmını atlayarak token'ı döndür
            return token;
        }
        return null; // Eğer header yoksa veya format yanlışsa null döndür
    }
}
