import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // ★ HttpOnly refresh 쿠키 전송용
});

// 공개 경로 목록 (baseURL 제외한 경로 기준)
const PUBLIC_PATHS = [
  /^\/users\/signup$/,
  /^\/users\/login$/,
  /^\/users\/refresh-token$/,   // ★ 리프레시 엔드포인트 일치!
  /^\/images\/.*/,              // /api/images/**
  /^\/dog-images\/.*/,          // /dog-images/**
  /^\/users\/check-email$/,
  /^\/users\/me\/check-nickname$/,
  /^\/spa-services\/.*/,        // /api/spa-services/**
  /^\/service-info\/?$/,        // /api/service-info
  /^\/reviews\/public\/.*/,     // /api/reviews/public/**
];

const isPublicPath = (url) => { // ← 타입 표기 제거
  if (!url) return false;
  try {
    // url이 절대/상대 섞여도 처리
    const u = new URL(url, 'http://dummy'); // 베이스는 의미 없음
    const path = u.pathname; // 쿼리 제외
    return PUBLIC_PATHS.some((re) => re.test(path));
  } catch {
    // URL 파싱 힘들면 그냥 문자열로 체크 (상대경로 가정)
    return PUBLIC_PATHS.some((re) => re.test(url));
  }
};

// ===== 토큰 보관소 =====
let accessToken = null;
(function boot() {
  const saved = localStorage.getItem('token');
  if (saved) accessToken = saved;
})();
function setAccessToken(token) {
  accessToken = token;
  if (token) localStorage.setItem('token', token);
  else localStorage.removeItem('token');
}
export { setAccessToken };


// ===== 로딩 표시 =====
let activeRequests = 0;

const showLoading = () => {
  activeRequests++;
  document.body.classList.add('loading');
};

const hideLoading = () => {
  activeRequests--;
  if (activeRequests <= 0) {
    document.body.classList.remove('loading');
  }
};

// ===== 요청 인터셉터 =====
api.interceptors.request.use(
  (config) => {
    showLoading();
    config.headers = config.headers || {};
    if (isPublicPath(config.url)) {
      delete config.headers.Authorization;
    } else if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => {
    hideLoading();
    return Promise.reject(error);
  }
);

// ===== 자동 리프레시 (동시요청 큐 처리) =====
let isRefreshing = false;
let queue = [];

async function callRefresh() {
  // ★ 백엔드 경로/키 이름 맞추기
  const resp = await axios.post(
    `${import.meta.env.VITE_API_BASE_URL}/api/users/refresh-token`,
    {},
    { withCredentials: true }
  );
  const newToken = resp?.data?.accessToken; // ← 키 바뀌면 여기 수정
  if (!newToken) throw new Error('No accessToken in refresh response');
  setAccessToken(newToken);
  return newToken;
}
function flushQueue(newToken) {
  queue.forEach((resolve) => resolve(newToken));
  queue = [];
}


// 중복 리다이렉트 방지
window.__alreadyRedirected = false;

api.interceptors.response.use(
  (res) => {
    hideLoading();
    return res;
  },
  async (err) => {
    hideLoading();

    const status = err.response?.status;
    const reqUrl = err.config?.url;
    const original = err.config || {};

    // 공개 경로면 그냥 실패시킴
    if (isPublicPath(reqUrl)) return Promise.reject(err);

    // 401/403 → 아직 재시도 안 했으면 리프레시 시도
    if ((status === 401 || status === 403) && !original._retry) {
      original._retry = true;

      if (!isRefreshing) {
        isRefreshing = true;
        try {
          const newToken = await callRefresh();
          flushQueue(newToken);
          // 재시도 시 새 토큰 주입
          original.headers = original.headers || {};
          original.headers.Authorization = `Bearer ${newToken}`;
          return api(original);
        } catch (e) {
          flushQueue(null);
          setAccessToken(null);
          if (!window.__alreadyRedirected) {
            window.__alreadyRedirected = true;
            alert('로그인 시간이 만료되었습니다. 다시 로그인해주세요.');
            setTimeout(() => (window.location.href = '/login'), 50);
          }
          return Promise.reject(e);
        } finally {
          isRefreshing = false;
        }
      } else {
        // 다른 요청이 refresh 중이면 큐에 대기
        return new Promise((resolve, reject) => {
          queue.push((newToken) => {
            if (!newToken) return reject(err);
            original.headers = original.headers || {};
            original.headers.Authorization = `Bearer ${newToken}`;
            resolve(api(original));
          });
        });
      }
    }

    // 그 외 에러
    return Promise.reject(err);
  }
);

export default api;