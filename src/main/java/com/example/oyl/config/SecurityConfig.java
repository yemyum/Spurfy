package com.example.oyl.config;

import com.example.oyl.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable()) // ⛔ 기본 로그인 폼 비활성화
                .httpBasic(basic -> basic.disable()) // ⛔ 기본 HTTP 인증 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/register", "/api/mypage").permitAll()
                        .anyRequest().authenticated()

                )
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);
                return http.build();


    }

}
