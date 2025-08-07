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

    public GptSpaRecommendationResponseDTO recommendSpa(SpaRecommendationRequestDTO dto) {
        log.info("GptClient.recommendSpa called with breed: '{}'", dto.getBreed());

        // 1. 사용자 입력 요약 텍스트 만들기
        String breedInfo = dto.getBreed() == null || dto.getBreed().isEmpty() ? "" : String.format("- 견종: %s\n", dto.getBreed());
        String ageGroupInfo = dto.getAgeGroup() == null || dto.getAgeGroup().isEmpty() ? "" : String.format("- 나이대: %s\n", dto.getAgeGroup());
        String skinTypesInfo = dto.getSkinTypes().isEmpty() ? "" : String.format("- 피부 상태: %s\n", String.join(", ", dto.getSkinTypes()));
        String healthIssuesInfo = dto.getHealthIssues().isEmpty() ? "" : String.format("- 건강 상태: %s\n", String.join(", ", dto.getHealthIssues()));
        String activityLevelInfo = dto.getActivityLevel() == null || dto.getActivityLevel().isEmpty() ? "" : String.format("- 활동성: %s\n", dto.getActivityLevel());

        String checklist = dto.getChecklist();
        String question = dto.getQuestion();

        // 2. 메시지 구성 (GPT에게 JSON 형식으로 응답 요청)
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("너는 \"스퍼피(spurfy)\"라는 반려견 힐링 스파 예약 시스템의 AI 친구야.\n");
        promptBuilder.append("보호자가 올려준 강아지 사진을 분석해서, 가장 어울리는 스파 서비스를 다정하게 추천해주는 역할을 해!\n\n");

        promptBuilder.append(String.format("사진 속 강아지는 '%s'로 인식됐고, 다음 정보들을 참고해서 스파를 추천할 거야: ", dto.getBreed()));
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

        Optional.ofNullable(checklist)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(c -> promptBuilder.append("## 보호자가 선택한 특징:\n")
                        .append(c).append("\n\n"));

        Optional.ofNullable(question)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(q -> promptBuilder.append("## 보호자의 추가 질문:\n")
                        .append(q).append("\n\n"));

        promptBuilder.append("보호자 입력 정보 중 없는 내용은 무시하고, 있는 내용만 참고하여 이 강아지에게 어울리는 스파를 추천해줘\n\n");
        promptBuilder.append("정보들을 바탕으로 이 강이지의 특징은 이렇구나~ 보호자는 이런걸 궁금해하구나~ 맥락으로 생각해줘\n\n");
        promptBuilder.append("⚠️ 아래 **강제 규칙**을 반드시 지켜. 하나라도 어기면 출력은 무효고, 재요청 대상이야.\n\n");
        promptBuilder.append("[강제 규칙]\n");
        promptBuilder.append("- \"견종: ~\", \"추천 스파: ~\" 같은 템플릿 문장 구성은 모두 금지야(=실패). 말하는 듯한 자연스러운 문장으로만 작성해줘.\n");
        promptBuilder.append("- 견종을 정확히 알 수 없을 경우, \"견종을 알 수 없다\", \"알 수 없는 견종\" 등의 문장을 절대 쓰지말고, 인상/특징 위주로 자연스럽게 말할 것\n");
        promptBuilder.append("- 견종을 알 수 없는 경우엔 견종이라는 단어조차 언급하지말 것\n");
        promptBuilder.append("- \"견종: 알 수 없는 ~\" 문장 절대 금지\n");
        promptBuilder.append("- \"추천 스파: ~\" 문장 절대 금지\n");
        promptBuilder.append("- 요약 형식(~~: ~~) 문장 절대 금지\n");
        promptBuilder.append("- 제일 첫 문장은 '사진 속의 아이는 **[견종]**(으)로 보이네요!' 형식으로, 견종이 인식되지 않으면 '사진 속 아이의 견종을 인식하지 못했어요..!'로 시작할 것 -> 이것을 'intro' 필드에 넣어줘.\n");
        promptBuilder.append("- 절대 강아지에게 존댓말 쓰지 마. 보호자에게만 존댓말!\n");
        promptBuilder.append("- \"성견이신 것 같아요\", \"알 수 없는 견종의 강아지\", \"주요 라벨\" 등 표현은 금지 (예: \"입력된 정보에 따르면\" 등도 금지)\n");
        promptBuilder.append("- 사용자 프롬프트 문장을 그대로 따라하지 마, 규칙을 지키는 선에서 자연스럽게 작성할 것\n");
        promptBuilder.append("- 나이, 견종 등은 추정하지 말고 중립적 표현 사용 (예: \"휴식이 필요한 아이\", \"피부가 민감한 친구\")\n");
        promptBuilder.append("- \"노령견\", \"시니어\", \"old dog\", \"고령\" 등 표현 사용 금지\n");
        promptBuilder.append("- 스파 이름은 **아래 목록 중에서만** 골라서, 이모지 + 마크다운 굵게로 출력할 것 (예: **\"🌿 카밍 스킨 스파\"**) -> 이것을 'spaName' 필드에 넣어줘.\n");
        promptBuilder.append("- 'spaName'에 해당하는 스파의 URL 친화적인 슬러그(slug) 값을 영어 소문자, 하이픈(-)으로만 구성하여 'spaSlug' 필드에 넣어줘. (예: '웰컴 스파' -> 'welcome-spa', '프리미엄 브러싱 스파' -> 'premium-brushing-spa', '릴렉싱 테라피 스파' -> 'relaxing-therapy-spa', '카밍 스킨 스파' -> 'calming-skin-spa')\n");
        promptBuilder.append("- 문장은 총 4~6줄 내외, 다정하지만 과장된 감성 멘트는 자제\n\n");

        promptBuilder.append("[추천 가능한 스파 목록]\n");
        promptBuilder.append("1. 🛁 웰컴 스파 – 처음 스파를 경험하는 아이들에게 딱, 기본 케어를 부드럽게 제공해요\n");
        promptBuilder.append("2. 🌸 프리미엄 브러싱 스파 – 고급 브러싱과 섬세한 손길로 보호자 만족도 최고!, 일상 속 색다른 스파용으로 추천\n");
        promptBuilder.append("3. 🧘‍♀️ 릴렉싱 테라피 스파 – 관절과 근육 이완, 활동성이 많은 아이들의 회복에 최고, 편안한 휴식이 필요한 아이에게 추천\n");
        promptBuilder.append("4. 🌿 카밍 스킨 스파 – 예민한 피부를 위한 순한 진정 스파, 저자극 제품 사용!\n\n");
        promptBuilder.append("※ 반드시 위 네 가지 스파 중에서 보호자님 강아지에게 가장 적합한 하나를 선택해서 \"spaName\"에 사용하고, 이모지와 이름을 정확히 복붙해서 써줘야 해. 새로운 이름을 만들어내면 안 돼!\n");

        promptBuilder.append("[문장 구조 규칙] 반드시 아래와 같은 JSON 형태로만 응답할 것! 다른 텍스트는 절대 포함하지 마!\n");
        promptBuilder.append("모든 값에는 줄바꿈(\\n)을 직접 넣어서 실제 화면 출력이 아래 예시처럼 나오도록 맞춰줘.\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"intro\": \"사진 속의 아이는 **%s**(으)로 보이네요!\\n소중한 반려견의 정보를 알려주셔서 감사해요!\\n\",\n".formatted(dto.getBreed()));
        promptBuilder.append("  \"compliment\": \"강아지에 대해 간략하게 칭찬 멘트 필요!\\n\\n\",\n");
        promptBuilder.append("  \"recommendationHeader\": \"이 아이에게 추천하는 스파는:\\n\\n\",\n");
        promptBuilder.append("  \"spaName\": \"**%s**에요!\\n\\n\",\n".formatted("스파 이름(이모지 포함)"));
        promptBuilder.append("  \"spaSlug\": \"스파 이름에 해당하는 슬러그 (예: welcome-spa)\",\n");
        promptBuilder.append("  \"spaDescription\": [\n");
        promptBuilder.append("    \"- 첫 번째 설명 (줄바꿈 포함)\",\n");
        promptBuilder.append("    \"- 두 번째 설명 (최대 2개, 줄바꿈 포함)\"\n");
        promptBuilder.append("  ],\n");
        promptBuilder.append("  \"closing\": \"\\n\\n다음 세 가지 중 하나를 선택해서 정확히 작성해줘: \\\"저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙\\\" OR \\\"소중한 반려견과 함께, 특별한 스파 시간을 보내보세요 💙\\\" OR \\\"우리 아이를 위한 힐링타임, 스퍼피가 함께할게요! 보호자님과 강아지 모두 편안한 시간이 되길 바라요 🐾\\\"\"\n");
        promptBuilder.append("}\n");

        promptBuilder.append("※ 아래 멘트는 참고용이며, 절대 그대로 베끼지 말고 규칙을 지키는 선에서 자연스럽게 새로 작성해줘.\n");
        promptBuilder.append("※ [출력 예시]\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"intro\": \"사진속의 아이는 **포메라니안**(으)로 보이네요!\\n소중한 반려견의 정보를 알려주셔서 감사해요!\\n\",\n");
        promptBuilder.append("  \"compliment\": \"털이 복실복실해서 에너지가 느껴지는 친구네요!\\n\\n\",\n");
        promptBuilder.append("  \"recommendationHeader\": \"이 아이에게 추천하는 스파는:\\n\\n\",\n");
        promptBuilder.append("  \"spaName\": \"**‍🧘‍♀️ 릴렉싱 테라피 스파**에요!\\n\\n\",\n");
        promptBuilder.append("  \"spaSlug\": \"relaxing-therapy-spa\",\n");
        promptBuilder.append("  \"spaDescription\": [\n");
        promptBuilder.append("    \"- 활동성이 많은 포메라니안에게 편안한 휴식을 제공해 주는 테라피 스파에요.\",\n");
        promptBuilder.append("    \"- 관절과 근육 이완을 위한 최적의 스파 서비스가 여기 있답니다.\"\n");
        promptBuilder.append("  ],\n");
        promptBuilder.append("\"closing\": \"선택한 멘트만 정확히 작성\"\n");
        promptBuilder.append("}\n");

        promptBuilder.append("※ 'spaDescription'은 리스트 형태로 2개의 설명을 포함해야 해. 각 설명은 '-'로 시작해야 해.\n");
        promptBuilder.append("※ 'intro' 필드에는 '사진 속의 아이는 **[견종]** (으)로 보이네요!' 형식으로 시작해야 해.\n");
        promptBuilder.append("※ 'spaName' 필드는 이모지 포함 마크다운 굵은 글씨로 스파 이름과 '에요!' 문장을 함께 작성해야 해. (예: **🌸 프리미엄 브러싱 스파**에요!)\n");
        promptBuilder.append("마지막 줄(closing 필드)은 아래 중 하나를 택해 그대로 출력할 것:\n");
        promptBuilder.append("- 저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙\n");
        promptBuilder.append("- 소중한 반려견과 함께, 특별한 스파 시간을 보내보세요 💙\n");
        promptBuilder.append("- 우리 아이를 위한 힐링타임, 스퍼피가 함께할게요! 보호자님과 강아지 모두 편안한 시간이 되길 바라요 🐾\n");
        promptBuilder.append("절대 안내문, 불필요한 텍스트, 선택지 설명을 closing 값에 포함하지 말 것.\n");
        promptBuilder.append("JSON 형식 외의 모든 텍스트는 출력 금지.\n");

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

        String labelsInfo = String.format("Google Vision API 라벨 분석 결과:\n- 주요 라벨: %s\n", String.join(", ", dto.getLabels()));

        // 1. 라벨 목록 요약 텍스트 만들기
        String ageGroupInfo = dto.getAgeGroup() == null || dto.getAgeGroup().isEmpty() ? "" : String.format("- 나이대: %s\n", dto.getAgeGroup());
        String skinTypesInfo = dto.getSkinTypes().isEmpty() ? "" : String.format("- 피부 상태: %s\n", String.join(", ", dto.getSkinTypes()));
        String healthIssuesInfo = dto.getHealthIssues().isEmpty() ? "" : String.format("- 건강 상태: %s\n", String.join(", ", dto.getHealthIssues()));
        String activityLevelInfo = dto.getActivityLevel() == null || dto.getActivityLevel().isEmpty() ? "" : String.format("- 활동성: %s\n", dto.getActivityLevel());

        String checklist = dto.getChecklist();
        String question = dto.getQuestion();

        // 2. 메시지 구성 (GPT에게 JSON 형식으로 응답 요청)
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("너는 \"스퍼피(spurfy)\"라는 반려견 힐링 스파 예약 시스템의 AI 친구야.\n");
        promptBuilder.append("보호자가 올려준 강아지 사진을 분석해서, 가장 어울리는 스파 서비스를 다정하게 추천해주는 역할을 해!\n\n");

        // 견종 인식 실패 시에도 사용자가 선택한 견종이 있다면 활용
        if (dto.getBreed() != null && !dto.getBreed().isEmpty() && !"알 수 없는 견종의 강아지".equals(dto.getBreed())) {
            promptBuilder.append(String.format("분석 결과, 명확한 견종은 인식되지 않았지만 보호자님이 직접 '%s' 견종이라고 알려주셨어! 다음 정보들을 참고해서 스파를 추천해줘: ", dto.getBreed()));
        } else {
            promptBuilder.append("분석 결과, 명확한 견종은 인식되지 않았지만, 다음 정보들을 참고해서 스파를 추천해줘: ");
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

        Optional.ofNullable(checklist)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(c -> promptBuilder.append("## 보호자가 선택한 특징:\n")
                        .append(c).append("\n\n"));

        Optional.ofNullable(question)
                .filter(s -> !s.trim().isEmpty())
                .ifPresent(q -> promptBuilder.append("## 보호자의 추가 질문:\n")
                        .append(q).append("\n\n"));

        promptBuilder.append("보호자 입력 정보 중 없는 내용은 무시하고, 있는 내용만 참고하여 이 강아지에게 어울리는 스파를 추천해줘\n");
        promptBuilder.append("정보들을 바탕으로 이 강이지의 특징은 이렇구나~ 보호자는 이런걸 궁금해하구나~ 맥락으로 생각해줘\n\n");
        promptBuilder.append("⚠️ 아래 **강제 규칙**을 반드시 지켜. 하나라도 어기면 출력은 무효고, 재요청 대상이야.\n\n");
        promptBuilder.append("[강제 규칙]\n");
        promptBuilder.append("- \"견종: ~\", \"추천 스파: ~\" 같은 템플릿 문장 구성은 모두 금지야(=실패). 말하는 듯한 자연스러운 문장으로만 작성해줘.\n");
        promptBuilder.append("- 견종을 정확히 알 수 없을 경우, \"견종을 알 수 없다\", \"알 수 없는 견종\" 등의 문장을 절대 쓰지말고, 인상/특징 위주로 자연스럽게 말할 것\n");
        promptBuilder.append("- 견종을 알 수 없는 경우엔 견종과 관련된 단어조차 언급하지말 것\n");
        promptBuilder.append("- \"견종: 알 수 없는 ~\" 문장 절대 금지\n");
        promptBuilder.append("- \"추천 스파: ~\" 문장 절대 금지\n");
        promptBuilder.append("- 요약 형식(~~: ~~) 문장 절대 금지\n");
        promptBuilder.append("- 제일 첫 문장은 \"사진 속 아이의 견종을 인식하지 못했어요..! 하지만 저희 스퍼피를 찾아와주신 보호자님을 위해 적절한 스파를 추천해드리고 싶어요!\"로 시작하거나, 보호자님이 견종을 알려주셨다면 '보호자님이 알려주신 견종은 **%s**이군요! 다음 정보들을 참고해서 스파를 추천해드릴게요!'로 시작할 것 -> 이것을 'intro' 필드에 넣어줘.\n".formatted(dto.getBreed() != null && !dto.getBreed().isEmpty() && !"알 수 없는 견종의 강아지".equals(dto.getBreed()) ? dto.getBreed() : ""));
        promptBuilder.append("- 절대 강아지에게 존댓말 쓰지 마. 보호자에게만 존댓말!\n");
        promptBuilder.append("- \"성견이신 것 같아요\", \"알 수 없는 견종의 강아지\", \"주요 라벨\" 등 표현은 금지 (예: \"입력된 정보에 따르면\" 등도 금지)\n");
        promptBuilder.append("- 나이, 견종 등은 추정하지 말고 중립적 표현 사용 (예: \"휴식이 필요한 아이\", \"피부가 민감한 친구\")\n");
        promptBuilder.append("- \"노령견\", \"시니어\", \"old dog\", \"고령\" 등 표현 사용 금지\n");
        promptBuilder.append("- 스파 이름은 **아래 목록 중에서만** 골라서, 이모지 + 마크다운 굵게로 출력할 것 (예: **\"🌿 카밍 스킨 스파\"**) -> 이것을 'spaName' 필드에 넣어줘.\n");
        promptBuilder.append("- 'spaName'에 해당하는 스파의 URL 친화적인 슬러그(slug) 값을 영어 소문자, 하이픈(-)으로만 구성하여 'spaSlug' 필드에 넣어줘. (예: '웰컴 스파' -> 'welcome-spa', '프리미엄 브러싱 스파' -> 'premium-brushing-spa', '릴렉싱 테라피 스파' -> 'relaxing-therapy-spa', '카밍 스킨 스파' -> 'calming-skin-spa')\n");
        promptBuilder.append("- 문장은 총 4~6줄 내외, 다정하지만 과장된 감성 멘트는 자제\n\n");

        promptBuilder.append("[추천 가능한 스파 목록]\n");
        promptBuilder.append("1. 🛁 웰컴 스파 – 처음 스파를 경험하는 아이들에게 딱, 기본 케어를 부드럽게 제공해요\n");
        promptBuilder.append("2. 🌸 프리미엄 브러싱 스파 – 고급 브러싱과 섬세한 손길로 보호자 만족도 최고!, 일상 속 색다른 스파용으로 추천\n");
        promptBuilder.append("3. 🧘‍♀️ 릴렉싱 테라피 스파 – 관절과 근육 이완, 활동성이 많은 아이들의 회복에 최고, 편안한 휴식이 필요한 아이에게 추천\n");
        promptBuilder.append("4. 🌿 카밍 스킨 스파 – 예민한 피부를 위한 순한 진정 스파, 저자극 제품 사용!\n\n");
        promptBuilder.append("※ 반드시 위 네 가지 스파 중에서 보호자님 강아지에게 가장 적합한 하나를 선택해서 \"spaName\"에 사용하고, 이모지와 이름을 정확히 복붙해서 써줘야 해. 새로운 이름을 만들어내면 안 돼!\n");

        promptBuilder.append("[문장 구조 규칙] (JSON 형식으로만 응답할 것! 다른 텍스트는 절대 포함하지 마!)\n");
        promptBuilder.append("모든 값에는 줄바꿈(\\n)을 직접 넣어서 아래 예시처럼 화면에 띄워질 형태로 맞춰줘.\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"intro\": \"%s\",\n".formatted(
                dto.getBreed() != null && !dto.getBreed().isEmpty() && !"알 수 없는 견종의 강아지".equals(dto.getBreed())
                        ? "보호자님이 알려주신 견종은 **%s**(이)군요!\\n소중한 반려견의 정보를 알려주셔서 감사해요!\\n".formatted(dto.getBreed())
                        : "사진 속 아이의 견종을 인식하지 못했어요..!\\n하지만 저희 스퍼피를 찾아와주신 보호자님을 위해 적절한 스파를 추천해드리고 싶어요!\\n"
        ));
        promptBuilder.append("  \"compliment\": \"강아지에 대해 간략하게 칭찬 멘트 필요!\\n\\n\",\n");
        promptBuilder.append("  \"recommendationHeader\": \"이 아이에게 추천하는 스파는:\\n\\n\",\n");
        promptBuilder.append("  \"spaName\": \"**%s**에요!\\n\\n\",\n".formatted("스파 이름(이모지 포함)"));
        promptBuilder.append("  \"spaSlug\": \"스파 이름에 해당하는 슬러그 (예: welcome-spa)\",\n");
        promptBuilder.append("  \"spaDescription\": [\n");
        promptBuilder.append("    \"- 첫 번째 설명 (줄바꿈 포함)\",\n");
        promptBuilder.append("    \"- 두 번째 설명 (최대 1~2개, 줄바꿈 포함)\"\n");
        promptBuilder.append("  ],\n");
        promptBuilder.append("\"closing\": \"선택한 멘트만 정확히 작성\"\n");
        promptBuilder.append("}\n");

        promptBuilder.append("※ 아래 멘트는 참고용이며, 절대 그대로 베끼지 말고 규칙을 지키는 선에서 자연스럽게 새로 작성해줘.\n");
        promptBuilder.append("※ [출력 예시]\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"intro\": \"보호자님이 알려주신 견종은 **포메라니안**(이)군요!\\n\",\n");
        promptBuilder.append("  \"compliment\": \"털이 복실복실해서 에너지가 느껴지는 친구네요!\\n\\n\",\n");
        promptBuilder.append("  \"recommendationHeader\": \"이 아이에게 추천하는 스파는:\\n\\n\",\n");
        promptBuilder.append("  \"spaName\": \"**‍🧘‍♀️ 릴렉싱 테라피 스파**에요!\\n\\n\",\n");
        promptBuilder.append("  \"spaSlug\": \"relaxing-therapy-spa\",\n");
        promptBuilder.append("  \"spaDescription\": [\n");
        promptBuilder.append("    \"- 활동성이 많은 포메라니안에게 편안한 휴식을 제공해 주는 테라피 스파에요.\",\n");
        promptBuilder.append("    \"- 관절과 근육 이완을 위한 최적의 스파 서비스가 여기 있답니다.\"\n");
        promptBuilder.append("  ],\n");
        promptBuilder.append("  \"closing\": \"\\n\\n저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙\"\n");
        promptBuilder.append("}\n");

        promptBuilder.append("※ 'spaDescription'은 리스트 형태로 2개의 설명을 포함해야 해. 각 설명은 '-'로 시작해야 해.\n");
        promptBuilder.append("※ 'intro' 필드는 견종이 인식되면 '보호자님이 알려주신 견종은 **[견종]**(이)군요!'로 시작하고, 인식되지 않으면 '사진 속 아이의 견종을 인식하지 못했어요..!'로 시작해야 해.\n");
        promptBuilder.append("※ 'spaName' 필드는 이모지 포함 마크다운 굵은 글씨로 스파 이름과 '에요!' 문장을 함께 작성해야 해. (예: **🌸 프리미엄 브러싱 스파**에요!)\n");
        promptBuilder.append("마지막 줄(closing 필드)은 아래 중 하나를 택해 그대로 출력할 것:\n");
        promptBuilder.append("- 저희 스퍼피에서 보호자님의 소중한 반려견과 함께 하는 스파 시간을 기다리고 있을게요 💙\n");
        promptBuilder.append("- 소중한 반려견과 함께, 특별한 스파 시간을 보내보세요 💙\n");
        promptBuilder.append("- 우리 아이를 위한 힐링타임, 스퍼피가 함께할게요! 보호자님과 강아지 모두 편안한 시간이 되길 바라요 🐾\n");
        promptBuilder.append("절대 안내문, 불필요한 텍스트, 선택지 설명을 closing 값에 포함하지 말 것.\n");
        promptBuilder.append("JSON 형식 외의 모든 텍스트는 출력 금지.\n");

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
            // GPT가 반환한 원시 JSON 문자열을 DTO 객체로 변환
            GptSpaRecommendationResponseDTO parsedResponse = objectMapper.readValue(gptRawResponse, GptSpaRecommendationResponseDTO.class);
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
            return "죄송해요! 지금은 스파 추천이 어려워요 \n조금 뒤에 다시 시도해 주세요!";
        }
        log.info("Raw content from GPT API: {}", response.getChoices().get(0).getMessage().getContent());
        return response.getChoices().get(0).getMessage().getContent();
    }

}
