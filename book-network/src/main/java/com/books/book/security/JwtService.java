package com.books.book.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secretKey}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    private String generateToken(Map<String, Object> claims, UserDetails userDetails){
      return buildToken(claims, userDetails, jwtExpiration);
    }

    private String buildToken (Map<String, Object> claims, UserDetails userDetails, Long jwtExpiration) {
        var authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setClaims(claims)
                .claim("Authorities", authorities)
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(getSignInKey())
                .compact();
    }

    public Boolean isTokenValid(String token, UserDetails userDetails){
        String username = extractUsername(token);
        return (userDetails.getUsername().equals(username) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token){
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        Claims claims = getAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims getAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    private Key getSignInKey () {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
