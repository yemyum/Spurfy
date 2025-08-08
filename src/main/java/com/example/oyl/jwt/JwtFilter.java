package com.example.oyl.jwt;

import com.example.oyl.domain.User;
import com.example.oyl.domain.UserRole;
import com.example.oyl.exception.CustomException;
import com.example.oyl.exception.ErrorCode;
import com.example.oyl.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private static final Key key = JwtUtil.getSigningKey();

    public JwtFilter(UserRepository userRepository) {
        this.userRepository = userRepository; // 주입
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/api/users/login") || path.equals("/api/users/signup");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String email = claims.getSubject();

                // DB에서 사용자 조회
                User user = userRepository.findByEmail(email)
                        .orElse(null);

                if (user != null) {
                    String role = UserRole.fromCode(user.getUserRole());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, List.of(() -> role));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    request.setAttribute("username", email);
                }

            } catch (ExpiredJwtException e) {
                logger.warn("JWT expired", e);
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                logger.warn("Invalid JWT", e);
                SecurityContextHolder.clearContext();
            }
        }

        // 무조건 다음 필터로 넘기기
        filterChain.doFilter(request, response);
    }
}
