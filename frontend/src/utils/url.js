// ✅ 절대 경로로 변환하는 함수
export const toAbs = (url) => {
  if (!url) return null;
  const s = String(url);
  if (/^(https?:|data:)/i.test(s)) return s;
  if (s.startsWith('blob:')) return s;
  const base = (import.meta.env.VITE_IMAGE_BASE_URL || '').replace(/\/$/, '');
  return `${base}${s.startsWith('/') ? '' : '/'}${s}`;
};