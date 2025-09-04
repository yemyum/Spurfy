export const normalize = (t) =>
    t ? String(t).replace(/\r\n/g, "\n").replace(/\\n/g, "\n") : "";

export const sanitizeText = (t) =>
    t ? t.replace(/\\n/g, "\n").replace(/\r\n/g, "\n").trim() : "";

export const prettifySentences = (t) =>
    sanitizeText(t).replace(/([.!?])\s+/g, "$1\n"); // ✅ sanitize → sanitizeText

export const parseTimestamp = (v) => {
    if (typeof v === "number" && Number.isFinite(v)) return v;
    const t = Date.parse(v);
    return Number.isFinite(t) ? t : Date.now();
};

export const formatSpaDescription = (spaDescription) => {
    const arr = Array.isArray(spaDescription) ? spaDescription.map(sanitizeText) : []; // ✅ sanitize → sanitizeText
    if (!arr.length) return "";

    return arr
        .map((line) => line.replace(/^[-*•]\s*/, "")) // 기존 불릿 제거
        .filter(Boolean)
        .map((line) => `- ${line}`)
        .join("\n");
};

export const formatAiMessage = (result) => {
    const intro = sanitizeText(result?.intro);
    const compliment = prettifySentences(result?.compliment || "");
    const header = sanitizeText(result?.recommendationHeader);
    const spaName = sanitizeText(result?.spaName);
    const spaDesc = formatSpaDescription(result?.spaDescription);
    const closing = sanitizeText(result?.closing);

    const text = result?.errorMessage
        ? sanitizeText(result.errorMessage)
        : [intro, compliment, header, spaName, spaDesc, closing]
            .filter(Boolean)
            .join("\n\n");

    return {
        text,
        spaSlug: result?.spaSlug ?? null,
        id: result?.id,
        timestamp: parseTimestamp(result?.createdAt),
        imageUrl: result?.imageUrl ?? null,
        errorMessage: result?.errorMessage ?? null,
    };
};