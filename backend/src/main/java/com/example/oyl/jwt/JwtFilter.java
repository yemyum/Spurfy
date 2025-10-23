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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private static final AntPathMatcher matcher = new AntPathMatcher();

    // 퍼블릭 경로 목록
    private static final List<String> SKIP_PATHS = List.of(
            "/api/users/login",
            "/api/users/signup",
            "/api/users/refresh-token",
            "/api/images/**",
            "/dog-images/**",
            "/api/users/check-email",
            "/api/users/me/check-nickname",
            "/api/spa-services/**",
            "/api/service-info",
            "/api/reviews/public/**"
    );

    public JwtFilter(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 경로 추출은 일관되게: getServletPath()가 혼동 적음
        final String path = request.getServletPath();
        final String method = request.getMethod();

        // 1) OPTIONS는 프리플라이트라 스킵 (정상)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("SKIP (OPTIONS) path={}", path);
            return true;
        }

        for (String p : SKIP_PATHS) {
            if (matcher.match(p, path)) {
                log.info("SKIP (WHITELIST) pattern={} path={}", p, path);
                return true;
            }
        }

        log.info("FILTER (PROTECTED) path={}", path);
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String path = req.getServletPath();
        final boolean isPublic = SKIP_PATHS.stream().anyMatch(p -> matcher.match(p, path));

        final String authHeader = req.getHeader("Authorization");

        // 보호 경로인데 토큰 자체가 없으면 바로 401
        if (!isPublic && (authHeader == null || !authHeader.startsWith("Bearer "))) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":\"A401\",\"message\":\"인증이 필요합니다.\"}");
            return;
        }

        // 토큰이 있으면 검증 시도
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseClaims(token);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String email = claims.getSubject();

                    userRepository.findByEmail(email).ifPresent(user -> {
                        String role = user.getUserRole().getRoleName();
                        var auth = new UsernamePasswordAuthenticationToken(
                                email, null, List.of(new SimpleGrantedAuthority(role))
                        );
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            } catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                if (!isPublic) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"code\":\"A401\",\"message\":\"토큰이 만료되었습니다.\"}");
                    return;
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                if (!isPublic) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"code\":\"A401\",\"message\":\"유효하지 않은 토큰입니다.\"}");
                    return;
                }
            }
        }

        chain.doFilter(req, res);
    }
}
