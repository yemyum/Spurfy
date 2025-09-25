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

        if (checklistJson == null || checklistJson.trim().isEmpty()) return result;

        try {
            Map<String, Object> parsed = mapper.readValue(checklistJson, Map.class);

            result.breed = getString(parsed, "selectedBreed", result.breed);
            result.ageGroup = getString(parsed, "ageGroup", result.ageGroup);
            result.activityLevel = getString(parsed, "activityLevel", result.activityLevel);
            result.healthIssues = getList(parsed.get("healthIssues"));

        } catch (IOException e) {
            // 로그는 서비스단에서 찍는 게 나음
        }

        return result;
    }

    private static String getString(Map<String,Object> map, String key, String defaultVal) {
        String v = (String) map.get(key);
        return (v != null && !v.isBlank() && !"선택 안 함".equals(v)) ? v.trim() : defaultVal;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getList(Object obj) {
        if (obj instanceof List<?> l && !l.isEmpty()) {
            return l.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return List.of();
    }
}
