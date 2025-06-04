package com.example.oyl.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;

public class JwtFilter extends OncePerRequestFilter {

    private static final Key key = JwtUtil.getSigningKey();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // JWT 인증이 필요한 URI만 필터 적용
        if (uri.startsWith("/api/mypage")) {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    request.setAttribute("username", claims.getSubject());
                } catch (Exception e) {
                    System.out.println("❌ 토큰 파싱 실패: " + e.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
                    return;
                }
            } else {
                System.out.println("⚠️ Authorization 헤더가 없거나 형식이 잘못됨");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization Header");
                return;
            }
        }

        // ✅ 필터는 무조건 마지막에 한 번만 호출
        filterChain.doFilter(request, response);
    }
}