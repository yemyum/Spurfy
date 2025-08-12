import React from 'react';
import ChecklistForm from "./ChecklistForm";
import SpurfyButton from "./SpurfyButton";

const ChecklistDrawer = ({
  sheetOpen,
  onClose,
  checklist,
  setChecklist,
  onChecklistSubmit,
  onApply,
}) => {
  if (!sheetOpen) return null;

  return (
    <>
      {/* 오버레이 */}
      <div className="fixed inset-0 bg-black/30 z-40" onClick={onClose} />

      {/* 데스크톱: 우측 드로어 */}
      <div className="hidden md:block fixed right-0 top-0 h-full w-[460px] bg-black/80 z-50 shadow-2xl">
        <div className="p-5 border-b border-gray-500 flex items-center justify-between">
          <h3 className="font-semibold text-gray-200">체크리스트 작성</h3>
          <button onClick={onClose} className="text-gray-500 font-semibold">닫기</button>
        </div>
        <div className="p-4 overflow-y-auto h-[calc(100%-56px)]">
          <ChecklistForm onSubmit={onChecklistSubmit} />
          <div className="mt-4 flex gap-2">
            <button
              onClick={() => {
                const empty = { ageGroup:'', activityLevel:'', selectedBreed:'', healthIssues:[] };
                setChecklist(empty);
              }}
              className="px-4 py-2 text-sm font-semibold bg-gray-500 text-gray-200 rounded-lg hover:bg-gray-600 transition duration-300"
            >
              초기화
            </button>
            <SpurfyButton
              variant="chat"
              onClick={onApply}
              className="px-4 py-2 text-sm"
            >
              적용
            </SpurfyButton>
          </div>
        </div>
      </div>

      {/* 모바일: 바텀시트 */}
      <div className="md:hidden fixed inset-x-0 bottom-0 z-50 rounded-t-2xl bg-black/80 shadow-2xl">
        <div className="p-4 border-b border-gray-500 flex items-center justify-between">
          <h3 className="font-semibold text-gray-200">체크리스트 작성</h3>
          <button onClick={onClose} className="text-gray-500 font-semibold">닫기</button>
        </div>
        <div className="p-4 max-h-[70vh] overflow-y-auto">
          <ChecklistForm onSubmit={onChecklistSubmit} />
          <div className="mt-4 flex justify-end gap-2">
            <button
              onClick={() => {
                const empty = { ageGroup:'', activityLevel:'', selectedBreed:'', healthIssues:[] };
                setChecklist(empty);
              }}
              className="px-4 py-2 text-sm font-semibold bg-gray-500 text-gray-200 rounded-lg hover:bg-gray-600 transition duration-300"
            >
              초기화
            </button>
            <SpurfyButton
              variant="chat"
              onClick={onApply}
              className="px-4 py-2 text-sm"
            >
              적용
            </SpurfyButton>
          </div>
        </div>
      </div>
    </>
  );
};

export default ChecklistDrawer;