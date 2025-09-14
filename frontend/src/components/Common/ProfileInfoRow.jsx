import React from 'react';

// 정보 표시 및 수정 필드 컴포넌트
function ProfileInfoRow({ label, value, isEditing, editedValue, onEditChange, type = "text" }) {
    if (label === "닉네임") { // 닉네임은 Profile 컴포넌트 내에서 직접 처리하므로 여기서 반환하지 않음
        return null;
    }
    return (
        <div>
            <label className="block mb-2">{label}</label>
            {isEditing && onEditChange ? (
                <input
                    type={type}
                    className="appearance-none w-full leading-tight focus:outline-none mb-2"
                    value={editedValue}
                    onChange={(e) => onEditChange(e.target.value)}
                    readOnly={label === "이메일"} // 이메일은 readOnly로 고정
                />
            ) : (
                <p className={`text-lg ${label === "이메일" ? "text-gray-400" : "text-gray-800"}`}>{value}</p>
            )}
        </div>
    );
}

export default ProfileInfoRow;