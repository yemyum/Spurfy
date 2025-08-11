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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private static final Key key = JwtUtil.getSigningKey();
    private static final AntPathMatcher matcher = new AntPathMatcher();

    // 퍼블릭 경로 목록
    private static final List<String> SKIP_PATHS = List.of(
            "/api/users/login",
            "/api/users/signup",
            "/api/images/**",
            "/dog-images/**",
            "/api/users/check-email",
            "/api/mypage/check-nickname",
            "/api/spa-services/**",
            "/api/service-info",
            "/api/reviews/public/**"
    );

    public JwtFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // CORS 프리플라이트는 스킵
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // 퍼블릭 경로는 스킵 (토큰 없어도 구경 가능)
        return SKIP_PATHS.stream().anyMatch(p -> matcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 토큰 없으면 인증만 세팅 안하고 통과
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

                String email = claims.getSubject();

            userRepository.findByEmail(email).ifPresent(user -> {
                // int -> "ROLE_USER" 같은 문자열 반환
                String authority = UserRole.fromCode(user.getUserRole());

                var auth = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority(authority))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.setAttribute("username", email);
            });

            } catch (ExpiredJwtException e) {
                log.debug("JWT expired: {}", e.getMessage());
                SecurityContextHolder.clearContext();     // 익명으로 진행
            } catch (Exception e) {
                log.debug("Invalid JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();     // 익명으로 진행
            }

        // 무조건 다음 필터로 넘기기
        filterChain.doFilter(request, response);
    }
}
