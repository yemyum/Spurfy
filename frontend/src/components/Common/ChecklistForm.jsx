import { useState, useEffect, useMemo, useRef } from "react";

function ChecklistForm({ onSubmit }) {
  const [ageGroup, setAgeGroup] = useState("");
  const [activityLevel, setActivityLevel] = useState("");
  const [selectedBreed, setSelectedBreed] = useState("");
  const [healthIssues, setHealthIssues] = useState([]);

  const breedOptions = [
    { label: "선택 안 함", value: "" },
    { label: "말티즈", value: "말티즈" },
    { label: "푸들", value: "푸들" },
    { label: "시츄", value: "시츄" },
    { label: "비숑 프리제", value: "비숑 프리제" },
    { label: "포메라니안", value: "포메라니안" },
    { label: "치와와", value: "치와와" },
    { label: "골든 리트리버", value: "골든 리트리버" },
    { label: "진돗개", value: "진돗개" },
    { label: "웰시코기", value: "웰시코기" },
    { label: "닥스훈트", value: "닥스훈트" },
    { label: "보더콜리", value: "보더콜리" },
    { label: "시바견", value: "시바견" },
    { label: "믹스견", value: "믹스견" },
  ];

  const healthIssuesOptions = [
    "피부 민감", "관절 약함", "알레르기", "심장 질환", "비만", "소화 문제", "귀 문제", "호흡기 문제"
  ];

  // 부모로 보낼 payload 정규화
  const payload = useMemo(() => ({
    ageGroup: (ageGroup || "").trim(),
    activityLevel: (activityLevel || "").trim(),
    selectedBreed: selectedBreed || "",
    healthIssues: Array.from(new Set(healthIssues)),
  }), [ageGroup, activityLevel, selectedBreed, healthIssues]);

  // 동일 payload 반복 호출 방지 (StrictMode 대비)
  const prevRef = useRef("");
  useEffect(() => {
    const now = JSON.stringify(payload);
    if (prevRef.current !== now) {
      onSubmit(payload);
      prevRef.current = now;
    }
  }, [payload, onSubmit]);

  const handleHealthIssuesChange = (e) => {
    const { value, checked } = e.target;
    setHealthIssues(prev => checked ? (prev.includes(value) ? prev : [...prev, value])
      : prev.filter(v => v !== value));
  };

  return (
    <form className="p-8 rounded-xl border-2 border-teal-200 bg-[#EFFFFE] space-y-6">

      {/* 견종 */}
      <div>
        <h2 className="font-bold text-xl mb-2">견종</h2>
        <select
          name="breed"
          value={selectedBreed ?? ""}   // 빈문자열이 "미선택"
          onChange={(e) => setSelectedBreed(e.target.value)}
          className="w-full p-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-100 transition duration-200"
        >
          {breedOptions.map((o) => (
            <option key={o.label} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>

      <div className="border-b-2 border-teal-100 my-4"></div>

      <div className="flex flex-col md:flex-row gap-4">

        {/* 나이대 */}
        <div className="flex-1"> {/* flex-1로 사용 가능한 공간을 균등하게 차지 */}
          <h2 className="font-bold text-xl mb-2">나이대</h2>
          <div className="flex flex-col space-y-2"> {/* 라디오 버튼 간격은 그대로 유지 */}
            {["생후 6개월 ~ 1년", "생후 1년 ~ 5년 이하", "생후 5년 이상"].map((option) => (
              <label key={option} className="flex items-center">
                <input
                  type="radio"
                  name="ageGroup"
                  value={option}
                  checked={ageGroup === option}
                  onChange={(e) => setAgeGroup(e.target.value)}
                  className="accent-[#3B82F6] w-4 h-4 mr-2"
                />
                {option}
              </label>
            ))}
          </div>
        </div>

        {/* 활동성 */}
        <div className="flex-1"> {/* flex-1로 사용 가능한 공간을 균등하게 차지 */}
          <h2 className="font-bold text-xl mb-2">활동성</h2>
          <div className="flex flex-col space-y-2"> {/* 라디오 버튼 간격은 그대로 유지 */}
            {["활발함", "보통", "차분함"].map((option) => (
              <label key={option} className="flex items-center">
                <input
                  type="radio"
                  name="activityLevel"
                  value={option}
                  checked={activityLevel === option}
                  onChange={(e) => setActivityLevel(e.target.value)}
                  className="accent-[#3B82F6] w-4 h-4 mr-2"
                />
                {option}
              </label>
            ))}
          </div>
        </div>
      </div>

      <div className="border-b-2 border-teal-100 my-4"></div>

      <div>
        <h2 className="font-bold text-xl mb-2 space-y-2">건강 상태/특징</h2>
        <div className="grid grid-cols-2 gap-2"> {/* 2열로 배치 */}
          {healthIssuesOptions.map((option) => (
            <label key={option} className="flex items-center">
              <input
                type="checkbox"
                name="healthIssues"
                value={option}
                checked={healthIssues.includes(option)}
                onChange={handleHealthIssuesChange}
                className="mr-2 accent-[#3B82F6] w-4 h-4"
              />
              {option}
            </label>
          ))}
        </div>
      </div>

    </form>
  );
}

export default ChecklistForm;
