import { useState, useEffect, useRef } from "react";

export const useChecklistStore = () => {
  const [checklist, setChecklist] = useState(() => {
    // 로컬스토리지에서 초기값 불러오기
    const saved = localStorage.getItem("spurfyChecklist");
    return saved ? JSON.parse(saved) : {};
  });

  const checklistRef = useRef(checklist);

  // state 바뀌면 ref & localStorage에 동기화
  useEffect(() => {
    checklistRef.current = checklist;
    localStorage.setItem("spurfyChecklist", JSON.stringify(checklist));
  }, [checklist]);

  return { checklist, setChecklist, checklistRef };
};