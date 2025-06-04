package com.example.my_site.controller;

import com.example.my_site.domain.User;
import com.example.my_site.jwt.JwtUtil;
import com.example.my_site.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        userService.register(user);
        return "회원가입 성공";

    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        boolean success = userService.login(user.getUsername(), user.getPassword());


        if (success) {
            String token = JwtUtil.createToken(user.getUsername());
            return Map.of(
                    "message", "로그인 성공",
                    "token", token
            );

        }else {
            return Map.of("message", "로그인 실패");

        }

    }

    @GetMapping("/mypage")
    public String mypage(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return username + "님의 마이페이지 입니다.";

    }
}
