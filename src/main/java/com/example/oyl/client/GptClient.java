package com.example.oyl.client;

import com.example.oyl.dto.GptRequestDTO;
import com.example.oyl.dto.GptResponseDTO;
import com.example.oyl.dto.SpaLabelRecommendationRequestDTO;
import com.example.oyl.dto.SpaRecommendationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GptClient {

    private final WebClient gptWebClient;

    public String recommendSpa(SpaRecommendationRequestDTO dto) {
        // 1. 사용자 입력 요약 텍스트 만들기
        String breedInfo = dto.getBreed() == null || dto.getBreed().isEmpty() ? "" : String.format("- 견종: %s\n", dto.getBreed());
        String ageGroupInfo = dto.getAgeGroup() == null || dto.getAgeGroup().isEmpty() ? "" : String.format("- 나이대: %s\n", dto.getAgeGroup());
        String skinTypesInfo = dto.getSkinTypes().isEmpty() ? "" : String.format("- 피부 상태: %s\n", String.join(", ", dto.getSkinTypes()));
        String healthConditionsInfo = dto.getHealthIssues().isEmpty() ? "" : String.format("- 건강 상태: %s\n", String.join(", ", dto.getHealthIssues()));
        String activityLevelInfo = dto.getActivityLevel() == null || dto.getActivityLevel().isEmpty() ? "" : String.format("- 활동성: %s\n", dto.getActivityLevel());

        String userInputSummary = String.format("""
            보호자가 입력한 정보:
            %s%s%s%s%s
            """,
                breedInfo,
                ageGroupInfo,
                skinTypesInfo,
                healthConditionsInfo,
                activityLevelInfo
        );

        // 2. 메시지 구성
        GptRequestDTO.Message message = new GptRequestDTO.Message();
        message.setRole("user");
        message.setContent(String.format("""
                너는 "스퍼피(spurfy)"라는 반려견 힐링 스파 예약 시스템의 AI 친구야.
                보호자가 올려준 강아지 사진을 분석해서, 가장 어울리는 스파 서비스를 다정하게 추천해주는 역할을 해!
        
                사진 속 강아지는 '%s'로 인식됐고, 다음 정보들을 참고해서 스파를 추천할 거야: %s
        
                보호자 입력 정보 중 없는 내용은 무시하고, 있는 내용만 참고하여 이 강아지에게 어울리는 스파를 추천해줘
      
                ⚠️ 아래 **강제 규칙**을 반드시 지켜. 하나라도 어기면 출력은 무효고, 재요청 대상이야.
        
                [강제 규칙]
                - "견종: ~", "추천 스파: ~" 같은 템플릿 문장 구성은 모두 금지야(=실패). 말하는 듯한 자연스러운 문장으로만 작성해줘.
                - 견종을 정확히 알 수 없을 경우, "견종을 알 수 없다", "알 수 없는 견종" 등의 문장을 절대 쓰지말고, 인상/특징 위주로 자연스럽게 말할 것
                - 견종을 알 수 없는 경우엔 견종이라는 단어조차 언급하지말 것
                - "견종: 알 수 없는 ~" 문장 절대 금지
                - "추천 스파: ~" 문장 절대 금지
                - 요약 형식(~~: ~~) 문장 절대 금지
                - 제일 첫 문장은 "사진 속의 아이는 "%s"로 보이네요!"와 비슷한 흐름으로 시작할 것 (예: 사진속의 아이는 말티즈로 보이는군요!)
                - 절대 강아지에게 존댓말 쓰지 마. 보호자에게만 존댓말!
                - "성견이신 것 같아요", "알 수 없는 견종의 강아지", "주요 라벨" 등 표현은 금지 (예: "입력된 정보에 따르면" 등도 금지)
                - 사용자 프롬프트 문장을 그대로 따라하지 마, 규칙을 지키는 선에서 자연스럽게 작성할 것
                - 나이, 견종 등은 추정하지 말고 중립적 표현 사용 (예: "휴식이 필요한 아이", "피부가 민감한 친구")
                - "노령견", "시니어", "old dog", "고령" 등 표현 사용 금지
                - 스파 이름은 **아래 목록 중에서만** 골라서, 이모지 + 마크다운 굵게로 출력할 것 (예: **"🌿 민감견 저자극 스파"**)
                - 문장은 총 4~6줄 내외, 다정하지만 과장된 감성 멘트는 자제
        
                [추천 가능한 스파 목록]
                1. 🛁 오리지널 스파 – 첫 스파 입문용, 누구에게나 잘 어울려요!
                2. 🌸 프리미엄 스파 – 섬세한 케어와 고급 브러싱이 필요한 친구들
                3. 🧘‍♀️ 시니어 릴렉싱 스파 – 관절 피로, 편안한 휴식이 필요한 아이
                4. 🌿 민감견 저자극 스파 – 피부가 예민하거나 알레르기 있는 친구들
        
                [문장 구조 규칙]("양식" 반드시 지킬것!, 각각 줄마다 줄바꿈 구분하기)
                ※ 아래 고정멘트 제외 예시 멘트는 단순한 참고용이며, 절대 그대로 베끼지 말고 유사한 분위기로 자연스럽게 바꿔서 말해줘.
                첫 번째 줄: 사진 속의 아이는 "%s"로 보이네요! (예: 사진속의 아이는 말티즈로 보이는군요!)
                두 번째 줄: 강아지에 대한 담백한 칭찬 (예: 사진 속 강아지의 생기 넘치는 모습이 인상 깊었어요!)
                세 번째 줄: 이 아이에게 추천하는 스파는: (고정멘트)
                네 번째 줄: 이모지 포함 마크다운 굵은 글씨로 스파 이름만 강조 (예: **"🌸 프리미엄 스파"**에요!)
                다섯~여섯 번째 줄: 해당 스파에 대한 간단한 설명 (줄바꿈 포함, "-" 형식으로 시작, 최대 2개까지 가능, 예: - 섬세한 브러싱과 고급 케어가 잘 어울릴 것 같아요.)
                마지막 줄: 아래 3개 중 하나를 선택해서 마무리할 것 (예: 저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙)
                   - 저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙
                   - 소중한 반려견과 함께, 특별한 스파 시간을 보내보세요 💙
                   - 우리 아이를 위한 힐링타임, 스퍼피가 함께할게요! 보호자님과 강아지 모두 편안한 시간이 되길 바라요 🐾

        """,  dto.getBreed(), userInputSummary));

        // 3. 요청 보내기
        GptRequestDTO request = new GptRequestDTO();
        request.setMessages(List.of(message));
        return callGptApi(request);
    }

    public String recommendSpaByLabels(SpaLabelRecommendationRequestDTO dto) {

        String labelsInfo = String.format("Google Vision API 라벨 분석 결과:\n- 주요 라벨: %s\n", String.join(", ", dto.getLabels()));

        // 1. 라벨 목록 요약 텍스트 만들기
        String ageGroupInfo = dto.getAgeGroup() == null || dto.getAgeGroup().isEmpty() ? "" : String.format("- 나이대: %s\n", dto.getAgeGroup());
        String skinTypesInfo = dto.getSkinTypes().isEmpty() ? "" : String.format("- 피부 상태: %s\n", String.join(", ", dto.getSkinTypes()));
        String healthConditionsInfo = dto.getHealthConditions().isEmpty() ? "" : String.format("- 건강 상태: %s\n", String.join(", ", dto.getHealthConditions()));
        String activityLevelInfo = dto.getActivityLevel() == null || dto.getActivityLevel().isEmpty() ? "" : String.format("- 활동성: %s\n", dto.getActivityLevel());

        String labelAndUserInputSummary = String.format("""
            %s%s%s%s%s
            """,
                labelsInfo, // labels 정보는 항상 들어감
                ageGroupInfo,
                skinTypesInfo,
                healthConditionsInfo,
                activityLevelInfo
        );

        // 2. 메시지 구성
        GptRequestDTO.Message message = new GptRequestDTO.Message();
        message.setRole("user");
        message.setContent(String.format("""
                너는 "스퍼피(spurfy)"라는 반려견 힐링 스파 예약 시스템의 AI 친구야.
                보호자가 올려준 강아지 사진을 분석해서, 가장 어울리는 스파 서비스를 다정하게 추천해주는 역할을 해!
                
                분석 결과, 명확한 견종은 인식되지 않았지만, 다음 정보들을 참고해서 스파를 추천해줘: %s
                보호자 입력 정보 중 없는 내용은 무시하고, 있는 내용만 참고하여 이 강아지에게 어울리는 스파를 추천해줘
                
                ⚠️ 아래 **강제 규칙**을 반드시 지켜. 하나라도 어기면 출력은 무효고, 재요청 대상이야.
                
                [강제 규칙]
                - "견종: ~", "추천 스파: ~" 같은 템플릿 문장 구성은 모두 금지야(=실패). 말하는 듯한 자연스러운 문장으로만 작성해줘.
                - 견종을 정확히 알 수 없을 경우, "견종을 알 수 없다", "알 수 없는 견종" 등의 문장을 절대 쓰지말고, 인상/특징 위주로 자연스럽게 말할 것
                - 견종을 알 수 없는 경우엔 견종이라는 단어조차 언급하지말 것
                - "견종: 알 수 없는 ~" 문장 절대 금지
                - "추천 스파: ~" 문장 절대 금지
                - 요약 형식(~~: ~~) 문장 절대 금지
                - 제일 첫 문장은 "사진 속 아이의 견종을 인식하지 못했어요..! 하지만 보호자님을 위해 저희 스파를 추천해드리고 싶어요!"와 비슷한 흐름으로 시작할 것
                - 절대 강아지에게 존댓말 쓰지 마. 보호자에게만 존댓말!
                - "성견이신 것 같아요", "알 수 없는 견종의 강아지", "주요 라벨" 등 표현은 금지 (예: "입력된 정보에 따르면" 등도 금지)
                - 사용자 프롬프트 문장을 그대로 따라하지 마, 규칙을 지키는 선에서 자연스럽게 작성할 것
                - 나이, 견종 등은 추정하지 말고 중립적 표현 사용 (예: "휴식이 필요한 아이", "피부가 민감한 친구")
                - "노령견", "시니어", "old dog", "고령" 등 표현 사용 금지
                - 스파 이름은 **아래 목록 중에서만** 골라서, 이모지 + 마크다운 굵게로 출력할 것 (예: **"🌿 민감견 저자극 스파"**)
                - 문장은 총 4~6줄 내외, 다정하지만 과장된 감성 멘트는 자제
                
                [추천 가능한 스파 목록]
                1. 🛁 오리지널 스파 – 첫 스파 입문용, 누구에게나 잘 어울려요!
                2. 🌸 프리미엄 스파 – 섬세한 케어와 고급 브러싱이 필요한 친구들
                3. 🧘‍♀️ 시니어 릴렉싱 스파 – 관절 피로, 편안한 휴식이 필요한 아이
                4. 🌿 민감견 저자극 스파 – 피부가 예민하거나 알레르기 있는 친구들
                
                [문장 구조 규칙]("양식" 반드시 지킬것!, 각각 줄마다 줄바꿈 구분하기)
                ※ 아래 고정멘트 제외 예시 멘트는 단순한 참고용이며, 절대 그대로 베끼지 말고 유사한 분위기로 자연스럽게 바꿔서 말해줘.
                첫 번째 줄: 사진 속 아이의 견종을 인식하지 못했어요..! 하지만 저희 스퍼피를 찾아와주신 보호자님을 위해 꼭 추천해드리고 싶어요! (고정멘트)
                두 번째 줄: 강아지에 대한 담백한 칭찬 (예: 사진 속 강아지의 생기 넘치는 모습이 인상 깊었어요!)
                세 번째 줄: 이 아이에게 추천하는 스파는: (고정멘트)
                네 번째 줄: 이모지 포함 마크다운 굵은 글씨로 스파 이름만 강조 (예: **"🌸 프리미엄 스파"**에요!)
                다섯~여섯 번째 줄: 해당 스파에 대한 간단한 설명 (줄바꿈 포함, "-" 형식으로 시작, 최대 2개까지 가능, 예: - 섬세한 브러싱과 고급 케어가 잘 어울릴 것 같아요.)
                마지막 줄: 아래 3개 중 하나를 선택해서 마무리할 것 (예: 저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙)
                   - 저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙
                   - 소중한 반려견과 함께, 특별한 스파 시간을 보내보세요 💙
                   - 우리 아이를 위한 힐링타임, 스퍼피가 함께할게요! 보호자님과 강아지 모두 편안한 시간이 되길 바라요 🐾
                """, labelAndUserInputSummary));

        // 3. GPT 요청 DTO 구성
        GptRequestDTO request = new GptRequestDTO();
        request.setMessages(List.of(message));
        return callGptApi(request);
    }

        // 4. GPT 호출
        private String callGptApi(GptRequestDTO request) {
        GptResponseDTO response = gptWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GptResponseDTO.class)
                .block();

        // GPT 응답 파싱 및 에러 처리 (기존 코드 그대로 가져오기)
        if (response == null ||
                response.getChoices() == null || response.getChoices().isEmpty() ||
                response.getChoices().get(0).getMessage() == null ||
                response.getChoices().get(0).getMessage().getContent() == null ||
                response.getChoices().get(0).getMessage().getContent().toLowerCase().contains("i'm sorry")) {
            return "죄송해요! 지금은 스파 추천이 어려워요 \n조금 뒤에 다시 시도해 주세요!";
        }
        return response.getChoices().get(0).getMessage().getContent();
    }

}
