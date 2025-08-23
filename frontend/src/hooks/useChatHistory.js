import { useState, useEffect, useCallback, useRef } from "react";
import api from "../api/axios";
import { toAbs } from '../utils/url';
import { upsert } from '../utils/upsert';

const formatAiMessage = (result) => {
  const s = (t) => (t ? t.replace(/\\n/g, "\n").replace(/\r\n/g, "\n").trim() : "");
  const parseTs = (v) => {
    if (typeof v === "number" && Number.isFinite(v)) return v;
    const t = Date.parse(v);
    return Number.isFinite(t) ? t : Date.now();
  };
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
    timestamp: parseTs(result.createdAt),
    imageUrl: result.imageUrl ?? null,
    errorMessage: result.errorMessage ?? null,
  };
};

export const useChatHistory = () => {
  const [chatMessages, setChatMessages] = useState([]);
  const MAX_LOCAL_ITEMS = 200;

  const sentRef = useRef(false);

  const sanitizeForStorage = (m) => ({
    id: m.id ?? Date.now(),
    timestamp: m.timestamp ?? Date.now(),
    isUser: !!m.isUser,
    text: m.text || "",
    imageUrl: m.imageUrl || null,
    spaSlug: m.spaSlug || null,
    errorMessage: m.errorMessage || null,
  });

  const addMessage = useCallback((newMessageObj) => {
    setChatMessages((prev) => {
      const next = [...prev, newMessageObj]; // ✅ 단순 append
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
  const replaceMessage = useCallback((oldId, newId, serverImg) => {
    setChatMessages((prev) => {
      const next = prev.map((msg) =>
        msg.id === oldId ? { ...msg, id: newId, imageUrl: toAbs(serverImg) } : msg
      );
      const store = next.filter((m) => m.isUser).map(sanitizeForStorage).slice(-MAX_LOCAL_ITEMS);
      localStorage.setItem("chatMessages", JSON.stringify(store));
      return next;
    });
  }, []);

  // ✅ 메시지를 삭제하는 함수 추가
  const removeMessage = useCallback((idToRemove) => {
    setChatMessages((prev) => {
      const next = prev.filter((msg) => msg.id !== idToRemove);
      const store = next.filter((m) => m.isUser).map(sanitizeForStorage).slice(-MAX_LOCAL_ITEMS);
      localStorage.setItem("chatMessages", JSON.stringify(store));
      return next;
    });
  }, []);

  useEffect(() => {
    if (sentRef.current) return;
    sentRef.current = true;

    const loadAndMergeMessages = async () => {
      let serverMsgs = [];
      let localMsgs = [];

      try {
        const { data } = await api.get("/recommendations/history");
        const items = data?.data ?? [];
        const serverMsgsFromApi = items.flatMap((item) => {
          const ai = formatAiMessage(item);
          const img = toAbs(item.imageUrl ?? item.image_url) || null;
          const promptText = (item.prompt ?? ai.prompt ?? "").trim();
          const msgs = [];
          if (promptText) {
            msgs.push({
              id: `user-${ai.id}`, // ✅ user- 접두사로 통일
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
            id: `ai-${ai.id}`,   // ✅ ai- 접두사로 통일
            imageUrl: null,
            isUser: false,
            errorMessage: item.errorMessage ?? null,
          });
          return msgs;
        });
        serverMsgs = serverMsgsFromApi;

        const savedLocal = JSON.parse(localStorage.getItem("chatMessages") || "[]");
        const localMsgsFromStorage = savedLocal.map((m) => ({
          ...m,
          id: m.id,
          imageUrl: m.imageUrl ? toAbs(m.imageUrl) : null,
        }));
        localMsgs = localMsgsFromStorage;
      } catch (e) {
        console.error("AI 기록 불러오기 실패:", e);
      }

      // ✅ SSOT: 서버가 진실. 로컬은 버리기!
      try { localStorage.removeItem("chatMessages"); } catch (e) { }

      let finalMessages = [...serverMsgs].sort(
        (a, b) => (a.timestamp || 0) - (b.timestamp || 0)
      );

      setChatMessages(finalMessages);

      // 유저 메시지만 다시 저장(선택)
      const store = finalMessages
        .filter(m => m.isUser)
        .map(sanitizeForStorage)
        .slice(-MAX_LOCAL_ITEMS);

      localStorage.setItem("chatMessages", JSON.stringify(store));
    };

    loadAndMergeMessages();
  }, []);

  return { chatMessages, addMessage, replaceImageUrl, replaceMessage, removeMessage };
};