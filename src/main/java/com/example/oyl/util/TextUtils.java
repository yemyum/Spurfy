package com.example.oyl.util;

public class TextUtils {
    public static String toAdjective(String s) {
        if (s == null) return "";
        return s.replaceAll("함$", "한");
    }

    public static String dedupeKo(String text) {
        if (text == null) return null;
        text = text.replaceAll("([가-힣]+)\\s*하고\\s*\\1(한|인|함)", "$1$2");
        text = text.replaceAll("([가-힣]+)\\s*하고\\s*\\1\\b", "$1");
        text = text.replaceAll("(\\b[가-힣]+)\\s+\\1(한|인|함)?", "$1$2");
        return text;
    }

    public static String normalizeSpaName(String raw) {
        if (raw == null) return null;
        return raw.replaceAll("(\\*\\*|[🧘‍♀️🌸🛁🌿]|\")", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }
}
