import { useState, useEffect } from "react";

function ChecklistForm({ onSubmit }) {
  const [ageGroup, setAgeGroup] = useState("");
  const [activityLevel, setActivityLevel] = useState("");
  const [selectedBreed, setSelectedBreed] = useState("");
  const [healthIssues, setHealthIssues] = useState([]);

  const breedOptions = [
    "선택 안 함", // 선택 안 함 옵션 추가
    "말티즈", "푸들", "시츄", "포메라니안", "치와와", "골든 리트리버", "시바견",
    "비숑 프리제", "웰시코기", "요크셔테리어", "닥스훈트", "믹스견", "보더콜리", "모름"
  ];

  const healthIssuesOptions = [
    "피부 민감", "관절 약함", "알레르기", "심장 질환", "비만", "소화 문제", "귀 문제", "호흡기 문제", "겁이 많음", "분리불안", "공격성", "활동량 부족"
  ];

  // ageGroup이나 activityLevel이 바뀔 때마다 부모 컴포넌트에 최신 데이터를 넘겨주기
  useEffect(() => {
    // ageGroup이나 activityLevel 중 하나라도 선택되면 (또는 초기 빈 값이라도)
    // 부모 컴포넌트의 onSubmit 함수를 호출해서 현재 상태를 전달
    onSubmit({ ageGroup, activityLevel, healthIssues, selectedBreed });
  }, [ageGroup, activityLevel, selectedBreed, healthIssues]);

  const handleHealthIssuesChange = (e) => {
    const { value, checked } = e.target;
    if (checked) {
      setHealthIssues((prev) => [...prev, value]); // 체크되면 배열에 추가
    } else {
      setHealthIssues((prev) => prev.filter((item) => item !== value)); // 체크 해제되면 배열에서 제거
    }
  };

  return (
    <form className="p-12 rounded-xl border-2 border-teal-200 bg-[#EFFFFE] space-y-6">

        {/* 견종 */}
        <div>
          <h2 className="font-bold text-xl mb-2">견종</h2>
          <select
            name="breed"
            value={selectedBreed}
            onChange={(e) => setSelectedBreed(e.target.value)}
            className="w-1/3 p-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-100 transition duration-200"
          >
            {breedOptions.map((option) => (
              <option key={option} value={option === "선택 안 함" ? "" : option}>
                {option}
              </option>
            ))}
          </select>
        </div>

      <div className="border-b-2 border-teal-100 my-4"></div>

      <div className="flex flex-col md:flex-row gap-4">

        {/* 나이대 */}
        <div className="flex-1"> {/* flex-1로 사용 가능한 공간을 균등하게 차지 */}
          <h2 className="font-bold text-xl mb-2">나이</h2>
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
