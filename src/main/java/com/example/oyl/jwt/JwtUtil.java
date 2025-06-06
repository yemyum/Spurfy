package com.example.oyl.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "my-super=secret-key-for-jwt-encoding!!";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간 (테스트용, 배포할 땐 30분~1시간으로 줄이기!)

    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    public static String createToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Key getSigningKey() {
        return key;

    }
}
