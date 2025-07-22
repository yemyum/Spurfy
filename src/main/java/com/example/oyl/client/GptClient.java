package com.example.oyl.client;

import com.example.oyl.domain.Dog;
import com.example.oyl.dto.GptRequestDTO;
import com.example.oyl.dto.GptResponseDTO;
import com.example.oyl.dto.SpaRecommendationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GptClient {

    private final WebClient gptWebClient;

    public String recommendSpa(SpaRecommendationRequestDTO dto) {
        // 1. 사용자 입력 요약 텍스트 만들기
        String userInputSummary = String.format("""
            보호자가 입력한 정보:
            - 견종: %s
            - 나이대: %s
            - 피부 상태: %s
            - 건강 상태: %s
            - 활동성: %s
            """,
                dto.getBreed(),
                dto.getAgeGroup(),
                dto.getSkinTypes().isEmpty() ? "특이사항 없음" : String.join(", ", dto.getSkinTypes()),
                dto.getHealthIssues().isEmpty() ? "특이사항 없음" : String.join(", ", dto.getHealthIssues()),
                dto.getActivityLevel()
        );

        // 2. 메시지 구성
        GptRequestDTO.Message message = new GptRequestDTO.Message();
        message.setRole("user");
        message.setContent(String.format("""
                안녕! 보호자님이 올려주신 사진 속 강아지는 '%s'로 보여요 🐶 \s
                이 견종에 대해 간단히 소개해주고, 아래 준비된 스파들 중에서 이 아이에게 어울리는 스파 2~3가지를 추천해줘요.
        
                [스파 종류]
                1. 🫧 오리지널 스파 – 반려견을 위한 가장 기본이 되는 케어, 첫 스파 입문용으로 추천!
                2. 🌸 프리미엄 스파 – 고급 오일과 브러싱이 포함된 프리미엄 구성, 섬세한 관리가 필요한 친구들에게 추천!
                3. 🧘‍♀️ 시니어 릴렉싱 스파 – 관절과 근육의 피로가 쌓인 노령견을 위한 릴렉싱 마사지, 편안한 휴식을 제공
                4. 🌿 민감견 저자극 스파 – 피부가 민감하거나 알레르기가 있는 반려견을 위한 저자극 스파, 무자극 자연 유래 제품만 사용해요.
        
                출력 형식은 아래처럼 해줘:
                ---
                사진 속의 아이는 '견종명'으로 보이는군요! 🐶 \s
                (간단한 견종 특징 설명)
        
                이 아이에게 어울리는 스파는:
                - 스파 이름 + 추천 이유
                - …
        
                (마무리: 따뜻하고 다정한 한 줄 멘트로 마무리해줘요! 예: "오늘도 뽀송뽀송 귀여운 하루 보내세요 💛")
                ---
        
                전체적인 톤은 보호자를 배려하는 따뜻하고 다정한 말투로, 너무 길지 않게 4~6줄 정도면 딱 좋아요!
        """, userInputSummary));

        // 3. 요청 보내기
        GptRequestDTO request = new GptRequestDTO();
        request.setMessages(List.of(message));

        // 4. GPT API 호출
        GptResponseDTO response = gptWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GptResponseDTO.class)
                .block();

        // 4. 응답에서 content만 추출해서 리턴
        return response.getChoices().get(0).getMessage().getContent();

    }
}
