export const formatDuration = (minutes) => {
  if (minutes >= 60) {
    const hours = Math.floor(minutes / 60);
    const remain = minutes % 60;
    return remain > 0
      ? `약 ${hours}시간 ${remain}분 소요`
      : `약 ${hours}시간 소요`;
  }
  return `약 ${minutes}분 소요`;
};