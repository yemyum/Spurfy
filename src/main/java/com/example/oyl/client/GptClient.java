package com.example.oyl.client;

import com.example.oyl.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptClient {

    private final WebClient gptWebClient;
    private final ObjectMapper objectMapper;

    private static final String CHECKLIST_NOT_SELECTED_BREED = "선택 안 함";

    public GptSpaRecommendationResponseDTO recommendSpa(SpaRecommendationRequestDTO dto) {
        log.info("GptClient.recommendSpa called with breed: '{}'", dto.getBreed());

        // 1. 사용자 입력 요약 텍스트 만들기
        // ✅ 프롬프트 필드 문자열 (빈값/선택 안 함이면 출력 생략)
        String breedInfo =
                isBlankOrNone(dto.getBreed()) ? "" : String.format("- 견종: %s\n", dto.getBreed());

        String ageGroupInfo =
                isBlankOrNone(dto.getAgeGroup()) ? "" : String.format("- 나이대: %s\n", dto.getAgeGroup());

        String activityLevelInfo =
                isBlankOrNone(dto.getActivityLevel()) ? "" : String.format("- 활동성: %s\n", dto.getActivityLevel());

        String skinTypesInfo =
                (dto.getSkinTypes() == null || dto.getSkinTypes().isEmpty())
                        ? ""
                        : String.format("- 피부 상태: %s\n", String.join(", ", dto.getSkinTypes()));

        String healthIssuesInfo =
                (dto.getHealthIssues() == null || dto.getHealthIssues().isEmpty())
                        ? ""
                        : String.format("- 건강 상태: %s\n", String.join(", ", dto.getHealthIssues()));

        String question  = dto.getQuestion();

        // 2. 메시지 구성 (GPT에게 JSON 형식으로 응답 요청)
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("너는 \"스퍼피(spurfy)\"라는 반려견 힐링 스파 예약 시스템의 AI '스피'야.\n");
        promptBuilder.append("보호자가 올려준 강아지 사진과 입력 정보들을 바탕으로, 자연스럽고 다정하게 어울리는 스파를 추천해줘.\n\n");

        promptBuilder.append(String.format("사진 속 강아지는 '%s'로 인식됐고, 다음 정보들을 참고해: ", dto.getBreed()));
        promptBuilder.append(String.format("""
            보호자가 입력한 정보:
            %s%s%s%s%s
            """,
                breedInfo,
                ageGroupInfo,
                skinTypesInfo,
                healthIssuesInfo,
                activityLevelInfo
        ));
        promptBuilder.append("\n");

        Optional.ofNullable(question)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(q -> promptBuilder.append("## 보호자의 추가 질문:\n")
                        .append(q).append("\n\n"));

        // 정보 처리 방식
        promptBuilder.append("- 보호자가 입력한 정보 중 제공되지 않은 항목은 무시하고, 제공된 정보만 기반으로 작성할 것.\n\n");

        // 강제 규칙 안내 (무조건 따라야 함)
        promptBuilder.append("⚠️ 아래는 강제 규칙. 하나라도 어기면 출력은 무효이며, 재요청 대상임.\n\n");

        // 금지 문장 형태
        promptBuilder.append("- \"견종: ~\", \"추천 스파: ~\"와 같은 템플릿형 요약 문장은 금지. → 자연스럽고 대화하듯 서술형 문장으로 작성할 것.\n");
        promptBuilder.append("- \"견종을 알 수 없다\", \"알 수 없는 견종\", \"입력된 정보에 따르면\" 등 설명조 문구 금지. → 대신 아이의 인상이나 분위기 중심으로 표현할 것.\n");
        promptBuilder.append("- \"추천 스파: ~\", \"요약: ~\"처럼 '~~: ~~' 형태의 요약 문장 금지.\n");
        promptBuilder.append("- \"성견이신 것 같아요\", \"주요 라벨\", \"고령견\", \"시니어\", \"old dog\" 등 GPT 내부 추론 또는 연령 언급 문구 금지.\n");

        // 표현 방식 규칙
        promptBuilder.append("- 강아지에게는 반드시 반말, 보호자에게만 존댓말을 사용할 것.\n");
        promptBuilder.append("- 강아지 이름은 절대 사용하지 말고, '이 아이', '이 친구', '반려견' 등의 중립적 표현을 사용할 것.\n");
        promptBuilder.append("- 나이, 견종 등은 추정하지 말고, '피부가 예민한 친구', '휴식이 필요한 아이' 등 중립적이고 포괄적인 묘사만 사용할 것.\n");
        promptBuilder.append("- 강아지 품종을 언급할 때는 반드시 자연스러운 조사를 붙여서 “포메라니안으로 보이네요!”와 같은 형식으로 작성할 것. (품종명에 받침이 있으면 '으로', 받침이 없으면 '로'를 붙임. 예: 푸들로, 포메라니안으로)\n");
        promptBuilder.append("- “문제를 가진”, “결함이 있는”, “이상한”, “장애가 있는” 등 부정적인 단어는 절대 사용하지 말 것.\n");
        promptBuilder.append("- 민감한 피부, 특별한 케어가 필요한 친구 등 부드러운 표현만 사용.\n");

        promptBuilder.append("- 아래 스파 목록 중 하나를 선택해 'spaName' 필드에 넣고, 반드시 이모지 + 마크다운 굵게(예: **\"🌿 카밍 스킨 스파\"**) 형식으로 출력할 것.\n");
        promptBuilder.append("- 해당 스파 이름을 기반으로 'spaSlug' 필드에는 영어 소문자+하이픈(-)으로 구성된 URL용 슬러그 값을 넣어줄 것. (예: '웰컴 스파' -> 'welcome-spa', '프리미엄 브러싱 스파' -> 'premium-brushing-spa', '릴렉싱 테라피 스파' -> 'relaxing-therapy-spa', '카밍 스킨 스파' -> 'calming-skin-spa')\n");
        promptBuilder.append("[스파 목록]\n");
        promptBuilder.append("1. 🛁 웰컴 스파 – 처음 스파를 경험하는 아이들에게 딱, 기본 케어를 부드럽게 제공해요\n");
        promptBuilder.append("2. 🌸 프리미엄 브러싱 스파 – 고급 브러싱과 섬세한 손길로 보호자 만족도 최고!, 일상 속 색다른 스파용으로 추천\n");
        promptBuilder.append("3. 🧘‍♀️ 릴렉싱 테라피 스파 – 관절과 근육 이완, 활동성이 많은 아이들의 회복에 최고, 편안한 휴식이 필요한 아이에게 추천\n");
        promptBuilder.append("4. 🌿 카밍 스킨 스파 – 예민한 피부를 위한 순한 진정 스파, 저자극 제품 사용!\n\n");

        promptBuilder.append("[응답 규칙] 반드시 순수 JSON 객체만 응답할 것. 마크다운 코드 블록(```), 백틱(`), 설명, 주석, 자연어는 절대 포함하지 말 것.\n");
        promptBuilder.append("응답은 반드시 '{' 로 시작하고 '}' 로 끝나는 JSON 객체여야 함.\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"intro\": \"사진 속 아이는 **%s**(으)로 보이네요!\\n소중한 반려견의 정보를 알려주셔서 감사합니다. 😊\\n\",\n".formatted(dto.getBreed()));
        promptBuilder.append("  \"compliment\": \"이 견종의 성격, 분위기, 보호자에게 어필할만한 특징을 1~2줄로 요약\\n\\n\",\n");
        promptBuilder.append("  \"recommendationHeader\": \"이 아이에게 추천하는 스파는\\n\\n\",\n");
        promptBuilder.append("  \"spaName\": \"**%s**에요!\\n\\n\",\n".formatted("스파 이름(이모지 포함)"));
        promptBuilder.append("  \"spaSlug\": \"스파 이름에 해당하는 슬러그 (예: welcome-spa)\",\n");
        promptBuilder.append("  \"spaDescription\": [\n");
        promptBuilder.append("    \"- 첫 번째 설명 (줄바꿈 포함)\",\n");
        promptBuilder.append("    \"- 두 번째 설명 (최대 2개, 줄바꿈 포함)\"\n");
        promptBuilder.append("  ],\n");
        promptBuilder.append("\"closing\": \"선택한 멘트만 정확히 작성\"\n");
        promptBuilder.append("}\n");

        promptBuilder.append("※ 'breed' 값이 비어있거나 \"알 수 없는 견종\"인 경우에도, 보호자에게 따뜻한 분위기의 스파를 진심을 담아 추천할 것.\n");
        promptBuilder.append("※ 제공된 JSON 구조와 규칙만 참고하며, 지침대로 새로운 응답을 생성할 것.\n");
        promptBuilder.append("※ 활동성 수식어는 최대 1회만 사용, 같은 의미의 형용사를 반복하지 말 것. 예) “활발하고 활발한” 금지.\n");
        promptBuilder.append("※ 체크리스트의 활동성(예: 활발함/보통/차분함)과 품종 기본 성향이 같다면 반드시 하나만 사용할 것.\n");

        promptBuilder.append("※ 'spaDescription' 필드는 문자열 배열 형태(List)이며, 정확히 2개의 설명 항목을 포함해야 함. 각 항목은 \"-\" 기호로 시작하는 한 줄짜리 설명이어야 함.\n");
        promptBuilder.append("※ 'spaName' 필드는 이모지를 포함하고, 마크다운 형식으로 굵게(**텍스트**) 표시된 스파 이름과 \"에요!\" 문장을 함께 포함해야 함. (예: **🌸 프리미엄 브러싱 스파**에요!)\n");
        promptBuilder.append("마지막 줄(closing 필드)은 반드시 아래 3가지 중 하나를 정확히 선택해 그대로 출력할 것:\n");
        promptBuilder.append("- 저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙\n");
        promptBuilder.append("- 소중한 반려견과 함께, 스퍼피와의 특별한 스파 시간을 보내보세요 💙\n");
        promptBuilder.append("- 우리 아이를 위한 힐링타임, 스퍼피가 함께할게요! 보호자님과 반려견 모두 편안한 시간이 되길 바래요 🐾\n");

        GptRequestDTO.Message message = new GptRequestDTO.Message();
        message.setRole("user");
        message.setContent(promptBuilder.toString());
        log.info("Sending prompt to GPT (recommendSpa):\n{}", promptBuilder.toString());

        // 3. 요청 보내기
        GptRequestDTO request = new GptRequestDTO();
        request.setMessages(List.of(message));
        return parseAndFormatGptResponse(callGptApi(request));
    }

    public GptSpaRecommendationResponseDTO recommendSpaByLabels(SpaLabelRecommendationRequestDTO dto) {
        log.info("GptClient.recommendSpaByLabels called with breed: '{}'", dto.getBreed());

        String labelsInfo = String.format("Google Vision API 라벨 분석 결과:\n- 주요 라벨: %s\n",
                String.join(", ", dto.getLabels())
        );

        // 1. 라벨 목록 요약 텍스트 만들기
        // ✅ 선택 안 함/빈값이면 자동 스킵
        String ageGroupInfo =
                isBlankOrNone(dto.getAgeGroup()) ? "" : String.format("- 나이대: %s\n", dto.getAgeGroup());

        String activityLevelInfo =
                isBlankOrNone(dto.getActivityLevel()) ? "" : String.format("- 활동성: %s\n", dto.getActivityLevel());

        String skinTypesInfo =
                (dto.getSkinTypes() == null || dto.getSkinTypes().isEmpty())
                        ? ""
                        : String.format("- 피부 상태: %s\n", String.join(", ", dto.getSkinTypes()));

        String healthIssuesInfo =
                (dto.getHealthIssues() == null || dto.getHealthIssues().isEmpty())
                        ? ""
                        : String.format("- 건강 상태: %s\n", String.join(", ", dto.getHealthIssues()));

        String question = dto.getQuestion();

        // 2. 메시지 구성 (GPT에게 JSON 형식으로 응답 요청)
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("너는 \"스퍼피(spurfy)\"라는 반려견 힐링 스파 예약 시스템의 AI '스피'야.\n");
        promptBuilder.append("보호자가 올려준 강아지 사진과 입력 정보들을 바탕으로, 자연스럽고 다정하게 어울리는 스파를 추천해줘.\n\n");

        // 견종 인식 실패 시에도 사용자가 선택한 견종이 있다면 활용
        String userBreed = Optional.ofNullable(dto.getSelectedBreed()).orElse("").trim();
        boolean hasUserBreed = !userBreed.isEmpty();
        String visionBreed = Optional.ofNullable(dto.getBreed()).orElse("").trim();

        if (hasUserBreed) {
            promptBuilder.append(
                    "보호자님이 '%s' 견종이라고 알려주셨어. 아래 정보를 참고해서 스파를 추천해줘: "
                            .formatted(userBreed)
            );
        } else {
            promptBuilder.append("아래 정보를 참고해서 스파를 추천해줘: ");
        }

        promptBuilder.append(String.format("""
            %s%s%s%s%s
            """,
                labelsInfo,
                ageGroupInfo,
                skinTypesInfo,
                healthIssuesInfo,
                activityLevelInfo
        ));
        promptBuilder.append("\n");

        Optional.ofNullable(question)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(q -> promptBuilder.append("## 보호자의 추가 질문:\n")
                        .append(q).append("\n\n"));

        // 정보 처리 방식
        promptBuilder.append("- 보호자가 입력한 정보 중 제공되지 않은 항목은 무시하고, 제공된 정보만 기반으로 작성할 것.\n\n");

        // 강제 규칙 안내 (무조건 따라야 함)
        promptBuilder.append("⚠️ 아래는 강제 규칙. 하나라도 어기면 출력은 무효이며, 재요청 대상임.\n\n");

        // 금지 문장 형태
        promptBuilder.append("- \"견종: ~\", \"추천 스파: ~\"와 같은 템플릿형 요약 문장은 금지. → 자연스럽고 대화하듯 서술형 문장으로 작성할 것.\n");
        promptBuilder.append("- \"견종을 알 수 없다\", \"알 수 없는 견종\", \"입력된 정보에 따르면\" 등 설명조 문구 금지. → 대신 아이의 인상이나 분위기 중심으로 표현할 것.\n");
        promptBuilder.append("- \"추천 스파: ~\", \"요약: ~\"처럼 '~~: ~~' 형태의 요약 문장 금지.\n");
        promptBuilder.append("- \"성견이신 것 같아요\", \"주요 라벨\", \"고령견\", \"시니어\", \"old dog\" 등 GPT 내부 추론 또는 연령 언급 문구 금지.\n");

        // 표현 방식 규칙
        promptBuilder.append("- 강아지에게는 반드시 반말, 보호자에게만 존댓말을 사용할 것.\n");
        promptBuilder.append("- 강아지 이름은 절대 사용하지 말고, '이 아이', '이 친구', '반려견' 등의 중립적 표현을 사용할 것.\n");
        promptBuilder.append("- 나이, 견종 등은 추정하지 말고, '피부가 예민한 친구', '휴식이 필요한 아이' 등 중립적이고 포괄적인 묘사만 사용할 것.\n");
        promptBuilder.append("- 강아지 품종을 언급할 때는 반드시 자연스러운 조사를 붙여서 “포메라니안으로 보이네요!”와 같은 형식으로 작성할 것. (품종명에 받침이 있으면 '으로', 받침이 없으면 '로'를 붙임. 예: 푸들로, 포메라니안으로)\n");
        promptBuilder.append("- “문제를 가진”, “결함이 있는”, “이상한”, “장애가 있는” 등 부정적인 단어는 절대 사용하지 말 것.\n");
        promptBuilder.append("- 민감한 피부, 특별한 케어가 필요한 친구 등 부드러운 표현만 사용.\n");

        promptBuilder.append("- 아래 스파 목록 중 하나를 선택해 'spaName' 필드에 넣고, 반드시 이모지 + 마크다운 굵게(예: **\"🌿 카밍 스킨 스파\"**) 형식으로 출력할 것.\n");
        promptBuilder.append("- 해당 스파 이름을 기반으로 'spaSlug' 필드에는 영어 소문자+하이픈(-)으로 구성된 URL용 슬러그 값을 넣어줄 것. (예: '웰컴 스파' -> 'welcome-spa', '프리미엄 브러싱 스파' -> 'premium-brushing-spa', '릴렉싱 테라피 스파' -> 'relaxing-therapy-spa', '카밍 스킨 스파' -> 'calming-skin-spa')\n");
        promptBuilder.append("[스파 목록]\n");
        promptBuilder.append("1. 🛁 웰컴 스파 – 처음 스파를 경험하는 아이들에게 딱, 기본 케어를 부드럽게 제공해요\n");
        promptBuilder.append("2. 🌸 프리미엄 브러싱 스파 – 고급 브러싱과 섬세한 손길로 보호자 만족도 최고!, 일상 속 색다른 스파용으로 추천\n");
        promptBuilder.append("3. 🧘‍♀️ 릴렉싱 테라피 스파 – 관절과 근육 이완, 활동성이 많은 아이들의 회복에 최고, 편안한 휴식이 필요한 아이에게 추천\n");
        promptBuilder.append("4. 🌿 카밍 스킨 스파 – 예민한 피부를 위한 순한 진정 스파, 저자극 제품 사용!\n\n");

        promptBuilder.append("[응답 규칙] 반드시 순수 JSON 객체만 응답할 것. 마크다운 코드 블록(```), 백틱(`), 설명, 주석, 자연어는 절대 포함하지 말 것.\n");
        promptBuilder.append("응답은 반드시 '{' 로 시작하고 '}' 로 끝나는 JSON 객체여야 함.\n");
        promptBuilder.append("{\n");
        boolean breedUnknown = visionBreed.isEmpty()
                || visionBreed.equals("알 수 없는 견종")
                || visionBreed.contains("알 수 없는")
                || visionBreed.equalsIgnoreCase("unknown")
                || visionBreed.toLowerCase(java.util.Locale.ROOT).contains("unidentified");

        // 체크리스트에서 최소 하나라도 들어왔는지
        boolean hasUserInfo =
                (dto.getAgeGroup() != null && !dto.getAgeGroup().isBlank()) ||
                        (dto.getActivityLevel() != null && !dto.getActivityLevel().isBlank()) ||
                        (dto.getHealthIssues() != null && !dto.getHealthIssues().isEmpty());
        String introMessage;
        if (hasUserBreed) {
            introMessage = "보호자님이 알려주신 견종은 **%s**(이)군요!\\n소중한 반려견의 정보를 알려주셔서 감사합니다. 😊\\n"
                    .formatted(userBreed);
        } else if (breedUnknown && !hasUserInfo) {
            // 사진도 모르고(unknown) 체크리스트도 없음 → 부드러운 안내
            introMessage = "반려견의 정확한 정보를 찾지 못했지만,\\n저 스피가 최적의 스파를 추천해드릴게요! 🤩\\n";
        } else {
            // 뭔가라도 정보가 있으면 중립/긍정 톤
            introMessage = "제공해주신 정보를 바탕으로, 사랑스러운 반려견에게 어울리는 스파를 추천해드릴게요. 😉\\n";
        }
        promptBuilder.append("  \"intro\": \"" + introMessage + "\",\n");
        promptBuilder.append("  \"compliment\": \"이 견종의 성격, 분위기, 보호자에게 어필할만한 특징을 1줄로 요약\\n\\n\",\n");
        promptBuilder.append("  \"recommendationHeader\": \"이 아이에게 추천하는 스파는\\n\\n\",\n");
        promptBuilder.append("  \"spaName\": \"**%s**에요!\\n\\n\",\n".formatted("스파 이름(이모지 포함)"));
        promptBuilder.append("  \"spaSlug\": \"스파 이름에 해당하는 슬러그 (예: welcome-spa)\",\n");
        promptBuilder.append("  \"spaDescription\": [\n");
        promptBuilder.append("    \"- 첫 번째 설명 (줄바꿈 포함)\",\n");
        promptBuilder.append("    \"- 두 번째 설명 (최대 1~2개, 줄바꿈 포함)\"\n");
        promptBuilder.append("  ],\n");
        promptBuilder.append("\"closing\": \"선택한 멘트만 정확히 작성\"\n");
        promptBuilder.append("}\n");

        promptBuilder.append("※ 'breed' 값이 비어있거나 \"알 수 없는 견종\"인 경우에도, 보호자에게 따뜻한 분위기의 스파를 진심을 담아 추천할 것.\n");
        promptBuilder.append("※ 제공된 JSON 구조와 규칙만 참고하며, 지침대로 새로운 응답을 생성할 것.\n");
        promptBuilder.append("※ 활동성 수식어는 최대 1회만 사용, 같은 의미의 형용사를 반복하지 말 것. 예) “활발하고 활발한” 금지.\n");
        promptBuilder.append("※ 체크리스트의 활동성(예: 활발함/보통/차분함)과 품종 기본 성향이 같다면 반드시 하나만 사용할 것.\n");

        promptBuilder.append("※ 'spaDescription' 필드는 문자열 배열 형태(List)이며, 정확히 2개의 설명 항목을 포함해야 함. 각 항목은 \"-\" 기호로 시작하는 한 줄짜리 설명이어야 함.\n");
        promptBuilder.append("※ 'spaName' 필드는 이모지를 포함하고, 마크다운 형식으로 굵게(**텍스트**) 표시된 스파 이름과 \"에요!\" 문장을 함께 포함해야 함. (예: **🌸 프리미엄 브러싱 스파**에요!)\n");
        promptBuilder.append("마지막 줄(closing 필드)은 반드시 아래 3가지 중 하나를 정확히 선택해 그대로 출력할 것:\n");
        promptBuilder.append("- 저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙\n");
        promptBuilder.append("- 소중한 반려견과 함께, 특별한 스파 시간을 보내보세요 💙\n");
        promptBuilder.append("- 우리 아이를 위한 힐링타임, 스퍼피가 함께할게요! 보호자님과 반려견 모두 편안한 시간이 되길 바래요 🐾\n");

        GptRequestDTO.Message message = new GptRequestDTO.Message();
        message.setRole("user");
        message.setContent(promptBuilder.toString());

        // 3. GPT 요청 DTO 구성
        GptRequestDTO request = new GptRequestDTO();
        request.setMessages(List.of(message));
        return parseAndFormatGptResponse(callGptApi(request));
    }

    // GPT 응답을 파싱하고 최종 문자열로 포맷
    private GptSpaRecommendationResponseDTO parseAndFormatGptResponse(String gptRawResponse) {
        log.info("Received raw GPT response: {}", gptRawResponse);
        try {
            // 전처리 단계
            String cleanedJson = gptRawResponse.trim();

            // 백틱 감싸짐 제거
            if (cleanedJson.startsWith("```") && cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(3, cleanedJson.length() - 3).trim();
            } else if (cleanedJson.startsWith("`") && cleanedJson.endsWith("`")) {
                cleanedJson = cleanedJson.substring(1, cleanedJson.length() - 1).trim();
            }

            // 시작이 `{` 인지 확인
            if (!cleanedJson.startsWith("{")) {
                log.warn("GPT 응답이 JSON 객체 형식이 아님. 시작: {}", cleanedJson.substring(0, Math.min(30, cleanedJson.length())));
                throw new IllegalArgumentException("응답이 JSON 형식이 아님");
            }

            // ✅ 파싱 시도
            // GPT가 반환한 원시 JSON 문자열을 DTO 객체로 변환
            GptSpaRecommendationResponseDTO parsedResponse = objectMapper.readValue(cleanedJson, GptSpaRecommendationResponseDTO.class);
            log.info("Parsed GPT response DTO: {}", parsedResponse);

            // DTO 객체를 그대로 반환
            return parsedResponse;

        } catch (Exception e) {
            log.error("GPT 응답 JSON 파싱 또는 포맷팅 실패: {}", gptRawResponse, e);

            // 파싱 실패 시 기본 오류 메시지를 담은 DTO 반환
            GptSpaRecommendationResponseDTO errorResponse = new GptSpaRecommendationResponseDTO();
            errorResponse.setIntro("죄송해요! 스파 추천 정보를 처리하는 데 문제가 발생했어요.");
            errorResponse.setCompliment("조금 뒤에 다시 시도해 주세요!");
            errorResponse.setRecommendationHeader("");
            errorResponse.setSpaName("");
            errorResponse.setSpaSlug("");
            errorResponse.setSpaDescription(List.of());
            errorResponse.setClosing("");
            return errorResponse;
        }
    }

        // 4. GPT 호출
        private String callGptApi(GptRequestDTO request) {
        GptResponseDTO response = gptWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GptResponseDTO.class)
                .block();

        // GPT 응답 파싱 및 에러 처리
        if (response == null ||
                response.getChoices() == null || response.getChoices().isEmpty() ||
                response.getChoices().get(0).getMessage() == null ||
                response.getChoices().get(0).getMessage().getContent() == null ||
                response.getChoices().get(0).getMessage().getContent().toLowerCase().contains("i'm sorry")) {
            log.error("GPT API 호출 결과 실패 또는 빈 응답: {}", response);
            return "죄송해요! 지금은 스파 추천이 어려워요. \n조금 뒤에 다시 시도해 주세요!";
        }
        log.info("Raw content from GPT API: {}", response.getChoices().get(0).getMessage().getContent());
        return response.getChoices().get(0).getMessage().getContent();
    }

    private static boolean isBlankOrNone(String s) {
        return s == null || s.isBlank() || CHECKLIST_NOT_SELECTED_BREED.equals(s.trim());
    }

}
