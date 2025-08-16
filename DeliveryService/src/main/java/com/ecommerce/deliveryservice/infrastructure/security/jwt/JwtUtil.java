package com.ecommerce.deliveryservice.infrastructure.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${my_secret_key}")
    private String my_secret_key;
    @Value("${myExpirationTime}")
    private long myExpirationTime;

    public String generateToken(String email, long userId,String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .claim("role", role) // Default role, can be changed based on your logic
                .claim("userId", userId) // Example userId, replace with actual logic to get userId
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

    public long extractUserId(String token){
        return Jwts.parser()
                .setSigningKey(my_secret_key)
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    public String getRole(HttpServletRequest request){
        String token = getTokenFromHeader(request);
        String role = extractRole(token);
        return "ROLE_" + role;
    }
    public String extractRole(String token){
        return Jwts.parser()
                .setSigningKey(my_secret_key)
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
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
            String token = authHeader.substring(7);
            return token;
        }
        return null;
    }
}
