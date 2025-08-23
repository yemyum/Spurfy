export function upsert(list, item) {
  // 배열이면 reduce로 돌려서 개별 upsert 처리
  if (Array.isArray(item)) {
    return item.reduce((acc, it) => upsert(acc, it), list);
  }

  const key = item.id;
  const existingIndex = list.findIndex(m => m.id === key);

  if (existingIndex !== -1) {
    const newList = [...list];
    newList[existingIndex] = { ...newList[existingIndex], ...item };
    return newList;
  } else {
    return [...list, item];
  }
}