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

        // ✅ 공통 블록 주입
        promptBuilder.append(commonPromptCore()).append("\n");

        // ✅ JSON 계약 주입 (성공 버전 → fallbackTone=false)
        String introMessage = "사진 속 아이는 **%s**(으)로 보이네요!\\n소중한 정보를 제공해주셔서 감사합니다. 😊\\n\\n"
                .formatted(dto.getBreed());

        promptBuilder.append(jsonContractBlock(introMessage, /*fallbackTone*/ false));

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

        List<String> labels = Optional.ofNullable(dto.getLabels()).orElse(List.of());
        String labelsInfo = "Google Vision API 라벨 분석 결과:\n- 주요 라벨: " +
                (labels.isEmpty() ? "없음" : String.join(", ", labels)) + "\n";

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

        promptBuilder.append(commonPromptCore()).append("\n");

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

        // 사용자 견종 선택 여부 기준으로 introMessage 분기
        String introMessage = hasUserBreed
                ? "보호자님께서 알려주신 견종은 **%s**(이)군요!\\n소중한 정보를 제공해주셔서 감사합니다. 😊\\n\\n".formatted(userBreed)
                : "정확한 견종은 찾지 못했지만,\\n제공해주신 정보를 바탕으로 반려견에게 어울리는 스파를 추천해드릴게요! 😉\\n\\n";

        promptBuilder.append(jsonContractBlock(introMessage, /*fallbackTone*/ true));

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
        String cleanedJson = gptRawResponse.trim();

        try {
            // 1-1. ASCII 제어 문자 제거 (NULL, Tab, LF, CR 등)
            cleanedJson = cleanedJson.replaceAll("[\\x00-\\x1F\\x7F]", "");

            // 1-2. 문자열 내의 이스케이프 안된 줄바꿈을 이스케이프된 줄바꿈으로 대체
            // GPT 응답에서 'spaDescription' 같은 배열 요소 안에 \n이 들어가는 것을 방지
            cleanedJson = cleanedJson.replace("\r", "\\r").replace("\n", "\\n");

            //  파서의 백틱 처리 보강
            if (cleanedJson.startsWith("```")) {
                int start = cleanedJson.indexOf("{");
                int end = cleanedJson.lastIndexOf("}");
                if (start >= 0 && end > start) {
                    cleanedJson = cleanedJson.substring(start, end + 1);
                }
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

            // (3개 인자 함수 호출)
            return GptSpaRecommendationResponseDTO.createFailureResponse(
                    "죄송해요! 스파 추천 정보를 처리하는 데 문제가 발생했어요.",
                    null,
                    "JSON_PARSE_ERROR"
            );
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
                response.getChoices().get(0).getMessage().getContent()
                        .toLowerCase(java.util.Locale.ROOT)
                        .contains("i'm sorry")) {
            log.error("GPT API 호출 결과 실패 또는 빈 응답: {}", response);
            return "죄송해요! 지금은 스파 추천이 어려워요. 조금 뒤에 다시 시도해 주세요!";
        }
        log.info("Raw content from GPT API: {}", response.getChoices().get(0).getMessage().getContent());
        return response.getChoices().get(0).getMessage().getContent();
    }

    private static boolean isBlankOrNone(String s) {
        return s == null || s.isBlank() || CHECKLIST_NOT_SELECTED_BREED.equals(s.trim());
    }

    // 공통 규칙 + 스파 목록
    private static String commonPromptCore() {
        return """
- 보호자가 입력한 정보 중 제공되지 않은 항목은 무시하고, 제공된 정보만 기반으로 작성할 것.

⚠️ 아래는 강제 규칙. 하나라도 어기면 출력은 무효이며, 재요청 대상임.

- "견종: ~", "추천 스파: ~"와 같은 템플릿형 요약 문장은 금지. → 자연스럽고 대화하듯 서술형 문장으로 작성할 것.
- "추천 스파: ~", "요약: ~"처럼 '~~: ~~' 형태의 요약 문장 금지.
- "성견이신 것 같아요", "주요 라벨", "고령견", "시니어", "old dog" 등 GPT 내부 추론 또는 연령 언급 문구 금지.

- 존댓말은 유지하 되, 높임말은 보호자에게만 사용할 것(강아지를 대상으로 높임말은 절대 금지).
- 강아지 이름은 절대 사용하지 말고, '이 아이', '이 친구', '반려견' 등의 중립적 표현을 사용할 것.
- 나이, 견종 등은 추정하지 말고, '피부가 예민한 친구', '휴식이 필요한 아이' 등 중립적이고 포괄적인 묘사, 쿠션어를 사용할 것.
- 강아지 품종을 언급할 때는 반드시 자연스러운 조사를 붙여서 작성할 것. (품종명 받침 규칙)
- “문제를 가진”, “결함이 있는”, “이상한”, “장애가 있는” 등 부정적인 단어는 절대 사용하지 말 것.

- 아래 스파 목록 중 하나를 선택해 'spaName' 필드에 넣고, 반드시 이모지 + 마크다운 굵게(예: **"🌿 카밍 스킨 스파"**) 형식으로 출력할 것.
- 해당 스파 이름을 기반으로 'spaSlug' 필드에는 영어 소문자+하이픈(-)으로 구성된 URL용 슬러그 값을 넣어줄 것. 
  (예: '웰컴 스파' -> 'welcome-spa', '프리미엄 브러싱 스파' -> 'premium-brushing-spa', '릴렉싱 테라피 스파' -> 'relaxing-therapy-spa', '카밍 스킨 스파' -> 'calming-skin-spa')

[스파 목록]
1. 🛁 웰컴 스파 – 처음 스파를 경험하는 아이들에게 딱, 기본 케어를 부드럽게 제공해요
2. 🌸 프리미엄 브러싱 스파 – 고급 브러싱과 섬세한 손길로 보호자 만족도 최고!, 일상 속 색다른 스파용으로 추천
3. 🧘‍♀️ 릴렉싱 테라피 스파 – 관절과 근육 이완, 활동성이 많은 아이들의 회복에 최고, 편안한 휴식이 필요한 아이에게 추천
4. 🌿 카밍 스킨 스파 – 예민한 피부를 위한 순한 진정 스파, 저자극 제품 사용!
""";
    }

    private static String jsonContractBlock(String introMessage, boolean fallbackTone) {
        StringBuilder b = new StringBuilder();
        b.append("[응답 규칙] 반드시 순수 JSON 객체만 작성할 것. 마크다운 코드 블록(```), 백틱(`), 설명, 주석, 자연어는 절대 포함하면 안됨!\n");
        b.append("응답은 반드시 '{' 로 시작하고 '}' 로 끝나는 JSON 객체여야 함.\n");
        b.append("※ 제공된 JSON 구조와 규칙만 참고하며, 지침대로 새로운 응답을 생성할 것.\n");
        b.append("※ 활동성 수식어는 최대 1회만 사용, 같은 의미의 형용사 반복 금지.\n");
        b.append("※ 체크리스트의 활동성과 품종 기본 성향이 같다면 반드시 하나만 사용할 것.\n");
        b.append("※ 'spaDescription' 필드는 문자열 배열(List) 형태이며, 정확히 2개의 설명 항목을 포함해야 함. 각 항목은 \"-\" 기호로 시작하는 한 줄짜리 설명으로 작성할 것.\n");
        b.append("※ 'spaName'은 이모지 포함 + **굵게 처리** + \"를 추천해요!\" 문장 포함.\n");
        b.append("{\n");
        b.append("  \"intro\": \"").append(introMessage).append("\",\n");
        b.append("  \"compliment\": \"이 견종의 성격, 분위기, 보호자에게 어필할만한 특징을 2줄로 요약\\n\\n\",\n");
        b.append("  \"recommendationHeader\": \"이런 성향의 아이에게는\\n\",\n");
        b.append("  \"spaName\": \"**스파 이름(이모지 포함)**를 추천할게요!\\n\\n\",\n");
        b.append("  \"spaSlug\": \"스파 이름에 해당하는 슬러그 (예: welcome-spa)\",\n");
        b.append("  \"spaDescription\": [\n");
        b.append("    \"- 첫 번째 설명 (해당 견종의 특성을 기반으로 한 설명)\",\n");
        b.append("    \"- 두 번째 설명 (그에 맞는 스파가 왜 어울리는지 설명)\"\n");
        b.append("  ],\n");
        b.append("  \"closing\": \"선택한 멘트만 정확히 작성\"\n");
        b.append("}\n");

        if (fallbackTone) {
            b.append("※ 'breed'가 불명확해도 따뜻하고 자신감 있는 톤으로 추천(메타 멘트 금지).\n");
        }

        // 클로징 후보 3개(두 버전 공통)
        b.append("마지막 줄(closing 필드)은 아래 3가지 중 하나만 선택해, 내용 그대로 'closing' 필드에 작성:\n");
        b.append("- 우리 아이와 함께하는 스파 시간, 스퍼피가 기다리고 있을게요. 💙\n");
        b.append("- 소중한 반려견과 함께, 오늘은 특별한 스파 데이 어떠세요? 💙\n");
        b.append("- 우리 아이의 힐링 타임, 스퍼피가 언제나 함께할게요. 🐾\n");
        return b.toString();
    }

}
