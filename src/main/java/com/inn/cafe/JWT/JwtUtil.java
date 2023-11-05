package com.inn.cafe.JWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import io.jsonwebtoken.io.Decoders; 
import io.jsonwebtoken.security.Keys; 

@Service
public class JwtUtil {
    
    private String secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    Claims claims;

    // Extracting username
    public String extractUserName(String token){
        return extractClaims(token, Claims::getSubject);
    }

    // Extracting Expiration date
    public Date extractExpiration(String token){
        return extractClaims(token,Claims::getExpiration);
    }

    // Extracting claims
    public <T> T extractClaims(String token, Function<Claims,T> claimsResolver){
        claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extracting all claims
    public Claims extractAllClaims(String token){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // Checking is token is expired
    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    // Generating token
    public String generateToken(String username, String role){
        Map<String,Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims,username);
    }

    // Creating token
    private String createToken(Map<String, Object> claims, String subject){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10 ))
                .signWith(SignatureAlgorithm.HS256, getSignKey()).compact();
    }

    private Key getSignKey() { 
        byte[] keyBytes= Decoders.BASE64.decode(secret); 
        return Keys.hmacShaKeyFor(keyBytes); 
    } 

    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isAdmin(){
        return "admin".equalsIgnoreCase((String) claims.get("role"));
    }

    public boolean isUser(){
        return "user".equalsIgnoreCase((String) claims.get("role"));
    }


}
