import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // ★ HttpOnly refresh 쿠키 전송용
});

// 공개 경로 목록 (baseURL 제외한 경로 기준)
const PUBLIC_PATHS = [
  /^\/users\/signup$/,
  /^\/users\/login$/,
  /^\/users\/refresh-token$/,
  /^\/images\/.*/,
  /^\/dog-images\/.*/,
  /^\/users\/check-email$/,
  /^\/users\/me\/check-nickname$/,
  /^\/spa-services\/.*/,
  /^\/service-info\/?$/,
  /^\/reviews\/public\/.*/,
];

const isPublicPath = (url) => {
  if (!url) return false;
  try {
    const u = new URL(url, 'http://dummy');
    return PUBLIC_PATHS.some((re) => re.test(u.pathname));
  } catch {
    return PUBLIC_PATHS.some((re) => re.test(url));
  }
};
const isRefreshPath = (url) => {
  try {
    const p = new URL(url, 'http://dummy').pathname;
    return /\/users\/refresh-token\/?$/.test(p);
  } catch {
    return /\/users\/refresh-token\/?$/.test(url || '');
  }
};

// ===== 토큰 보관소 =====
let accessToken = null;

(function boot() {
  try {
    const saved = localStorage.getItem('token');
    if (saved) {
      accessToken = saved;
      api.defaults.headers.common = api.defaults.headers.common || {};
      api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
    }
  } catch { }
})();

export function setAccessToken(token) {
  accessToken = token || null;
  if (token) {
    try { localStorage.setItem('token', token); } catch { }
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    try { localStorage.removeItem('token'); } catch { }
    delete api.defaults.headers.common.Authorization;
  }
}

// 명시적 로그아웃 중 리프레시 금지 플래그
let forceLogout = false;
export function beginForceLogout() { forceLogout = true; }

// 다탭 동기화: 다른 탭에서 token 바뀌면 기본헤더도 갱신
window.addEventListener('storage', (e) => {
  if (e.key === 'token') {
    setAccessToken(e.newValue || null);
  }
});

// ===== 로딩 표시 =====
let activeRequests = 0;

const showLoading = () => {
  activeRequests++;
  document.body.classList.add('loading');
};

const hideLoading = () => {
  activeRequests = Math.max(0, activeRequests - 1);
  if (activeRequests === 0) document.body.classList.remove('loading');
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
function flushQueue(newToken) {
  queue.forEach((resolve) => resolve(newToken));
  queue = [];
}

async function callRefresh() {
  const resp = await api.post('/users/refresh-token', {}, { withCredentials: true });
  const newToken =
    resp?.data?.data?.accessToken ??
    resp?.data?.accessToken ??
    resp?.data?.data ??
    null;
  if (!newToken) throw new Error('No accessToken in refresh response');
  setAccessToken(newToken);
  return newToken;
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

    // 공개/리프레시 경로는 재시도 안 함
    if (isPublicPath(reqUrl) || isRefreshPath(reqUrl)) {
      return Promise.reject(err);
    }

    // 명시적 로그아웃 중엔 리프레시 금지
    if (forceLogout) {
      return Promise.reject(err);
    }

    // 401 → 아직 재시도 안 했으면 리프레시 시도
    if (status === 401 && !original._retry) {
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
            window.location.href = '/login';
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