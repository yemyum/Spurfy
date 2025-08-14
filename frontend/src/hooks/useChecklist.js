import { useState, useRef } from "react";

export const useChecklist = () => {
  const [sheetOpen, setSheetOpen] = useState(false);
  // ✅ localStorage에서 초기값 불러오기
  const savedChecklist = JSON.parse(localStorage.getItem("checklistData") || "null");
  const [checklist, setChecklist] = useState({
    ageGroup: '',
    activityLevel: '',
    selectedBreed: '선택 안 함', // 초기값
    healthIssues: []
  });
  const checklistDataRef = useRef(savedChecklist || null);

  // ✅ checklist 변경 시 localStorage에도 저장
  useEffect(() => {
    localStorage.setItem("checklistData", JSON.stringify(checklist));
  }, [checklist]);

  const isNotSelected = (v) => !v || v.trim() === "선택 안 함";

  const selectedCount =
    (isNotSelected(checklist.selectedBreed) ? 0 : 1) +
    (checklist.ageGroup ? 1 : 0) +
    (checklist.activityLevel ? 1 : 0) +
    (checklist.healthIssues?.length || 0);

  const syncChecklistToRef = () => {
    checklistDataRef.current = {
      ...(checklistDataRef.current || {}),
      ...(checklist || {}),
    };
  };

  const handleChecklistSubmit = (data) => {
    console.log("체크리스트 데이터 변경:", data);
    setChecklist(data);            // state & localStorage에 저장
    checklistRef.current = data;   // 최신값 ref에도 반영
  };

  const handleApplyChecklist = () => {
    checklistRef.current = checklist; // state 값 ref에 동기화
    setSheetOpen(false);
  };

  const handleResetChecklist = () => {
    const empty = { ageGroup: '', activityLevel: '', selectedBreed: '선택 안 함', healthIssues: [] };
    setChecklist(empty);
    checklistDataRef.current = empty;
    localStorage.removeItem("checklistData"); // ✅ 저장값 제거
  };

  return {
    sheetOpen,
    setSheetOpen,
    checklist,
    setChecklist,
    checklistDataRef,
    selectedCount,
    handleChecklistSubmit,
    handleApplyChecklist,
    handleResetChecklist,
  };
};