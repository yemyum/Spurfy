import { useState, useEffect, useCallback } from "react";
import api from "../api/axios";

const toAbs = (u) => {
  if (!u) return null;
  if (/^(https?:|data:|blob:)/i.test(u)) return u;
  const base = (import.meta.env.VITE_IMAGE_BASE_URL || "").replace(/\/$/, "");
  return `${base}${u.startsWith("/") ? "" : "/"}${u}`;
};

const formatAiMessage = (result) => {
  const s = (t) => (t ? t.replace(/\\n/g, "\n").replace(/\r\n/g, "\n").trim() : "");
  const spaDesc =
    Array.isArray(result.spaDescription) && result.spaDescription.length
      ? result.spaDescription.map((line) => `- ${s(line).replace(/^- /, "")}`).join("\n")
      : "";

  const text = result.errorMessage
    ? s(result.errorMessage)
    : [s(result.intro), s(result.compliment), s(result.recommendationHeader), s(result.spaName), spaDesc, s(result.closing)]
      .filter(Boolean)
      .join("\n\n");

  return {
    text: text,
    spaSlug: result.spaSlug ?? null,
    id: result.id,
    timestamp: new Date(result.createdAt).getTime(),
    imageUrl: result.imageUrl ?? null,
    errorMessage: result.errorMessage ?? null,
  };
};

export const useChatHistory = () => {
  const [chatMessages, setChatMessages] = useState([]);
  const MAX_LOCAL_ITEMS = 200;

  const sanitizeForStorage = (m) => ({
    id: m.id ?? Date.now(),
    timestamp: m.timestamp ?? Date.now(),
    isUser: !!m.isUser,
    text: m.text || "",
    imageUrl: null, // 로컬스토리지는 이미지 저장 안 함
    spaSlug: m.spaSlug || null,
    errorMessage: m.errorMessage || null,
    checklist: m.checklist || null,
  });

  const addMessage = useCallback((newMessageObj) => {
    setChatMessages((prev) => {
      const msg = { id: newMessageObj.id || Date.now(), timestamp: newMessageObj.timestamp || Date.now(), ...newMessageObj };
      const next = [...prev, msg];
      const store = next.filter((m) => m.isUser).map(sanitizeForStorage).slice(-MAX_LOCAL_ITEMS);
      localStorage.setItem("chatMessages", JSON.stringify(store));
      return next;
    });
  }, []);

  const replaceImageUrl = useCallback((idToUpdate, newImageUrl) => {
    setChatMessages((prev) => {
      const next = prev.map((msg) =>
        msg.id === idToUpdate ? { ...msg, imageUrl: toAbs(newImageUrl) } : msg
      );
      const store = next.filter((m) => m.isUser).map(sanitizeForStorage).slice(-MAX_LOCAL_ITEMS);
      localStorage.setItem("chatMessages", JSON.stringify(store));
      return next;
    });
  }, []);

  // ✅ 메시지의 ID를 교체하는 함수 추가
  const replaceMessageId = useCallback((oldId, newId, serverImg) => {
    setChatMessages((prev) => {
      const next = prev.map((msg) =>
        msg.id === oldId ? { ...msg, id: newId, imageUrl: toAbs(serverImg) } : msg
      );
      const store = next.filter((m) => m.isUser).map(sanitizeForStorage).slice(-MAX_LOCAL_ITEMS);
      localStorage.setItem("chatMessages", JSON.stringify(store));
      return next;
    });
  }, []);

  useEffect(() => {
    const loadAndMergeMessages = async () => {
      let serverMsgs = [];
      try {
        const { data } = await api.get("/recommendations/history");
        const items = data?.data ?? [];

        serverMsgs = items.flatMap((item) => {
          const ai = formatAiMessage(item);
          const img = toAbs(item.imageUrl ?? item.image_url) || null;
          const promptText = (item.prompt ?? ai.prompt ?? "").trim();
          const msgs = [];

          if (promptText) {
            msgs.push({
              id: `prompt-${ai.id}`,
              timestamp: ai.timestamp - 1,
              isUser: true,
              text: promptText,
              imageUrl: img,
              checklist: null,
              spaSlug: null,
              errorMessage: null,
            });
          }

          msgs.push({
            ...ai,
            imageUrl: null,
            isUser: false,
            errorMessage: item.errorMessage ?? null,
          });
          return msgs;
        });
      } catch (e) {
        console.error("AI 기록 불러오기 실패:", e);
      }

      // ✅ 서버 프롬프트 '텍스트' 집합(이미지는 로컬에 저장 안 하므로 텍스트만 비교)
      const serverPromptTexts = new Set(
        serverMsgs
          .filter((m) => m.isUser && typeof m.text === "string")
          .map((m) => m.text.trim())
          .filter((t) => t.length > 0)
      );

      const savedLocal = JSON.parse(localStorage.getItem("chatMessages") || "[]");
      const localMsgs = savedLocal.map((m) => ({
        ...m,
        imageUrl: m.imageUrl ? toAbs(m.imageUrl) : null,
      }));

      const finalMap = new Map();

      // 서버 우선
      serverMsgs.forEach((m) => {
        finalMap.set(m.id, m);
      });

      // 로컬 병합: 서버에 동일 '텍스트' 프롬프트가 있으면 스킵
      localMsgs.forEach((m) => {
        if (m.isUser && typeof m.text === "string") {
          const t = m.text.trim();
          if (t.length > 0 && serverPromptTexts.has(t)) {
            return; // ← 여기서 중복 프롬프트 컷
          }
        }
        if (!finalMap.has(m.id)) {
          finalMap.set(m.id, m);
        }
      });

      const sorted = Array.from(finalMap.values()).sort((a, b) => a.timestamp - b.timestamp);
      setChatMessages(sorted);
    };

    loadAndMergeMessages();
  }, [addMessage]);

  return { chatMessages, addMessage, replaceImageUrl, replaceMessageId };
};