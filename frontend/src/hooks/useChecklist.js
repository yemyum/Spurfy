import { useState, useRef } from "react";

export const useChecklist = () => {
  const [sheetOpen, setSheetOpen] = useState(false);
  const [checklist, setChecklist] = useState({
    ageGroup:'', activityLevel:'', selectedBreed:'', healthIssues:[]
  });
  const checklistDataRef = useRef(null);

  const selectedCount = (checklist.selectedBreed ? 1 : 0)
    + (checklist.ageGroup ? 1 : 0)
    + (checklist.activityLevel ? 1 : 0)
    + (checklist.healthIssues?.length || 0);

  const syncChecklistToRef = () => {
    checklistDataRef.current = {
      ...(checklistDataRef.current || {}),
      ...(checklist || {}),
    };
  };

  const handleChecklistSubmit = (data) => {
    console.log("체크리스트 데이터 변경:", data);
    checklistDataRef.current = data;
    setChecklist(data);
  };
  
  const handleApplyChecklist = () => {
    syncChecklistToRef();
    setSheetOpen(false);
  };

  const handleResetChecklist = () => {
    const empty = { ageGroup:'', activityLevel:'', selectedBreed:'', healthIssues:[] };
    setChecklist(empty);
    checklistDataRef.current = empty;
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