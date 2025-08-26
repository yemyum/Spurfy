export const formatDuration = (minutes) => {
  if (minutes >= 60) {
    const hours = Math.floor(minutes / 60);
    const remain = minutes % 60;
    return remain > 0
      ? `약 ${hours}시간 ${remain}분`
      : `약 ${hours}시간`;
  }
  return `약 ${minutes}분`;
};