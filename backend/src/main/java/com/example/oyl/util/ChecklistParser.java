package com.example.oyl.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChecklistParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ChecklistResult parse(String checklistJson) {
        ChecklistResult result = new ChecklistResult();
        result.ageGroup = "성견";  // 기본값
        result.activityLevel = "보통";

        if (checklistJson == null || checklistJson.trim().isEmpty()) return result;  // json 글자 뭉치가 null이라면 기본값으로 리턴

        try {
            Map<String, Object> parsed = mapper.readValue(checklistJson, Map.class); // json을 map(key, value) 형태로 쪼갬

            result.breed = getString(parsed, "selectedBreed", result.breed);
            result.ageGroup = getString(parsed, "ageGroup", result.ageGroup);
            result.activityLevel = getString(parsed, "activityLevel", result.activityLevel);
            result.healthIssues = getList(parsed.get("healthIssues"));

        } catch (IOException e) {
            // 로그는 서비스단에서 찍는 게 나음
        }
        return result;
    }

    // 문자열 청소 (아래 3가지 조건 중 하나라도 걸리면 기본값 유지, 정상적인 글자일 때만 이쁘게 돌려줌)
    private static String getString(Map<String,Object> map, String key, String defaultVal) {
        String v = (String) map.get(key);
        return (v != null && !v.isBlank() && !"선택 안 함".equals(v)) ? v.trim() : defaultVal; // 값이 없거나, 공백만 있거나, '선택 안 함'일 경우 -> 기본값 반환
    }

    // 여러 개를 체크했을 시 정제
    private static List<String> getList(Object obj) {
        if (obj instanceof List<?> l && !l.isEmpty()) {  // 1. 리스트 형태가 맞고 비어있지 않다면?
            return l.stream()                            // [참고] stream의 역할: 데이터들이 줄을 서서 컨베이어 벨트 위로 하나씩 올라가게 함
                    .filter(Objects::nonNull)            // 2. 리스트 안에 null이 있으면 버림
                    .map(String::valueOf)                // 3. 혹시 다른 타입일 수 있으니 확실하게 문자열로 변환
                    .map(String::trim)                   // 4. 글자 앞 뒤 공백 청소
                    .filter(s -> !s.isEmpty())     // 5. 공백 제거했는데, 빈 글자라면 탈락
                    .toList();                           // 6. 살아남은 데이터들만 새 리스트에 담아 리턴
        }
        return List.of();   // 리스트 형식이 아니거나 비어있으면 안전하게 빈 리스트로 리턴!
    }
}
