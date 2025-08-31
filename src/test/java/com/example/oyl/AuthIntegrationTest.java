package com.example.oyl;

import com.example.oyl.repository.RefreshTokenRepository;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.jayway.jsonpath.JsonPath;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll(); // 테스트 시작 전 모든 토큰 삭제
    }

    @Test
    void 로그인_토큰_발급만_테스트() throws Exception {
        // 이미 DB에 가입된 계정 정보
        String email = "t@t.com";
        String password = "1234";

        // 로그인 → 토큰 발급 확인!
        MvcResult login = mvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists()) // accessToken 잘 오는지 확인
                .andReturn();

        // 여기서 accessToken 뽑기!
        String oldAccessToken = JsonPath.read(login.getResponse().getContentAsString(), "$.data");

        Cookie refresh = login.getResponse().getCookie("refreshToken");
        assertNotNull(refresh, "refreshToken 쿠키가 응답에 없음");

        // 👇 디버그 출력 추가
        System.out.println("쿠키 원문 = " + refresh.getValue());
        System.out.println("쿠키 sha256 = " + sha256(refresh.getValue()));

        refreshTokenRepository.findAll()
                .forEach(t -> System.out.println("DB token_hash = " + t.getTokenHash()));
        Thread.sleep(2100);

        // 3. 보호 API → 401 (토큰 만료)
        mvc.perform(get("/api/dogs")
                        .header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isUnauthorized());

        // 4. 리프레시로 accessToken 재발급
        MvcResult ref = mvc.perform(post("/api/users/refresh-token")
                        .cookie(new Cookie("refreshToken", refresh.getValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        String newAccess = JsonPath.read(ref.getResponse().getContentAsString(), "$.data");

        // 5. 새 accessToken으로 보호 API 재호출 → 200 OK
        mvc.perform(get("/api/dogs")
                        .header("Authorization", "Bearer " + newAccess))
                .andExpect(status().isOk());
    }

    // 테스트 전용 sha256
    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
