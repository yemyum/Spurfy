package com.example.oyl.jwt;

import com.example.oyl.domain.User;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Component
public class JwtUtil {

    private final Key key;
    private final long accessExpSeconds;
    private final long refreshExpDays;

    public JwtUtil(
            @Value("${jwt.access-exp-seconds}") long accessExpSeconds,
            @Value("${jwt.refresh-exp-days}") long refreshExpDays
    ) {
        this.key = Keys.hmacShaKeyFor("my-super=secret-key-for-jwt-encoding!!".getBytes(StandardCharsets.UTF_8));
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpDays = refreshExpDays;
    }

    // 토큰 발급
    public String createAccessToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpSeconds * 1000);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("nickname", user.getNickname())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpDays * 24 * 60 * 60 * 1000L);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getUserId())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 검증/파싱
    // 유효하지 않으면 CustomException 던짐 (리프레시 검증에 그대로 사용)
    public void validateTokenOrThrow(String token) {
        try {
            parseClaims(token); // 파싱 성공 = 유효
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    // 클레임 파싱 (만료/서명 포함 검증)
    public Claims parseClaims(String token) {
        String t = normalize(token);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60) // 시계 오차 허용
                .build()
                .parseClaimsJws(t)
                .getBody();
    }

    // 토큰 만료 체크 (깨진 토큰 -> 만료 취급)
    public boolean isExpired(String token) {
        try {
            Date exp = parseClaims(token).getExpiration();
            return exp.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // DB 저장용 리프레시 만료 시각 계산
    public LocalDateTime calcRefreshExpiresAt() {
        return LocalDateTime.now().plusDays(refreshExpDays);
    }

    // 내부 헬퍼
    private String normalize(String token) {
        if (token == null) return "";
        String t = token.trim();
        if (t.startsWith("Bearer ")) t = t.substring(7); // 액세스 헤더용 대비
        return t;
    }

}
