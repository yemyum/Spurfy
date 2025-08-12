import { useState, useEffect } from "react";
import api from '../api/axios';

const formatAiMessage = (result) => {
  const sanitizeText = (t) => (t ? t.replace(/\\n/g, '\n').replace(/\r\n/g, '\n').trim() : '');
  const spaDescription = (result.spaDescription && result.spaDescription.length > 0)
    ? result.spaDescription.map(line => `- ${sanitizeText(line).replace(/^- /, '')}`).join('\n')
    : '';

  return {
    text: [
      sanitizeText(result.intro),
      sanitizeText(result.compliment),
      sanitizeText(result.recommendationHeader),
      sanitizeText(result.spaName),
      spaDescription,
      sanitizeText(result.closing)
    ].filter(Boolean).join('\n\n'),
    spaSlug: result.spaSlug,
    id: result.id,
    timestamp: new Date(result.createdAt).getTime(),
    imageUrl: result.imageUrl || null
  };
};

export const useChatHistory = () => {
  const [chatMessages, setChatMessages] = useState([]);

  useEffect(() => {
    const loadAndMergeMessages = async () => {
      let serverAiMessages = [];
      try {
        const response = await api.get('/recommendations/history');
        if (response.data?.data) {
          serverAiMessages = response.data.data.map(item => ({
            ...formatAiMessage(item),
            isUser: false
          }));
        }
      } catch (error) {
        console.error('AI 기록 불러오기 실패:', error);
      }

      const savedLocalMessages = JSON.parse(localStorage.getItem("chatMessages")) || [];
      const localMessages = savedLocalMessages.map(msg => {
        if (!msg.text && msg.message) {
          msg.text = msg.message;
          delete msg.message;
        }
        return {
          ...msg,
          imageUrl: msg.imageUrl || null,
          imageBase64: msg.imageBase64 || null
        };
      });

      const uniqueLocalMessages = localMessages.filter(m => m.isUser);
      const combinedMessages = [...uniqueLocalMessages, ...serverAiMessages];
      const finalMap = new Map();
      combinedMessages.forEach(msg => finalMap.set(msg.id, msg));
      const sorted = Array.from(finalMap.values()).sort((a, b) => a.timestamp - b.timestamp);
      setChatMessages(sorted);
    };
    loadAndMergeMessages();
  }, []);

  const addMessage = (newMessageObj) => {
    setChatMessages((prev) => {
      const msg = {
        id: newMessageObj.id || Date.now(),
        timestamp: newMessageObj.timestamp || Date.now(),
        ...newMessageObj,
      };
      const msgForStorage = { ...msg };
      localStorage.setItem("chatMessages", JSON.stringify([...prev, msgForStorage]));
      return [...prev, msg];
    });
  };

  return { chatMessages, addMessage };
};