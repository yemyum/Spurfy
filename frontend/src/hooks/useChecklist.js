import { useState, useEffect, useMemo } from "react";

export const useChecklist = () => {
  const [sheetOpen, setSheetOpen] = useState(false);
  const [checklist, setChecklist] = useState({
    ageGroup: '',
    activityLevel: '',
    selectedBreed: '선택 안 함', // 초기값
    healthIssues: []
  });

  const isNotSelected = (v) => !v || v.trim() === "선택 안 함";

  // ✅ 최소 하나라도 선택됐는지
  const hasAnySelection = useMemo(() => {
    return (
      !isNotSelected(checklist.selectedBreed) ||
      (checklist.ageGroup && checklist.ageGroup.trim() !== "") ||
      (checklist.activityLevel && checklist.activityLevel.trim() !== "") ||
      (Array.isArray(checklist.healthIssues) && checklist.healthIssues.length > 0)
    );
  }, [checklist]);

  const selectedCount =
    (isNotSelected(checklist.selectedBreed) ? 0 : 1) +
    (checklist.ageGroup ? 1 : 0) +
    (checklist.activityLevel ? 1 : 0) +
    (checklist.healthIssues?.length || 0);

  const handleChecklistSubmit = (data) => {
    console.log("체크리스트 데이터 변경:", data);
    setChecklist(data);
  };

  const handleApplyChecklist = () => {
    setSheetOpen(false);
  };

  const handleResetChecklist = () => {
    const empty = { ageGroup: '', activityLevel: '', selectedBreed: '선택 안 함', healthIssues: [] };
    setChecklist(empty);
  };

  // ✅ 백엔드로 보낼 “정규화된” 오브젝트
  const toPayload = () => {
    const norm = (s) => (s && s.trim() !== "선택 안 함" ? s.trim() : "");
    return {
      selectedBreed: norm(checklist.selectedBreed),   // "" 이면 서버에서 무시
      ageGroup: norm(checklist.ageGroup),
      activityLevel: norm(checklist.activityLevel),
      healthIssues: Array.isArray(checklist.healthIssues) ? checklist.healthIssues : [],
      // 서버 DTO에 skinTypes 필드가 있다면 비워서 같이 보내도 OK
      skinTypes: []
    };
  };

  return {
    sheetOpen,
    setSheetOpen,
    checklist,
    setChecklist,
    selectedCount,
    hasAnySelection,
    toPayload,
    handleChecklistSubmit,
    handleApplyChecklist,
    handleResetChecklist,
  };
};