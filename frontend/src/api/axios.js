import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// 공개 경로 목록 (baseURL 제외한 경로 기준)
const PUBLIC_PATHS = [
  /^\/reviews\/public\//,  // /api + /reviews/public/**
  /^\/spa-services\//,      // /api + /spa-services/**
  /^\/service-info\/?$/
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

// ===== [로딩 상태 관리] =====
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

// ===== [요청 인터셉터] =====
api.interceptors.request.use(
  (config) => {
    // 로딩 시작
    showLoading();

    const token = localStorage.getItem('token');

    // headers 보장 (Axios v1에서도 객체/AxiosHeaders 둘 다 안전하게)
    if (!config.headers) config.headers = {};

    if (isPublicPath(config.url)) {
      // 공개 경로면 Authorization 제거
      // AxiosHeaders 객체일 수도 있으므로 둘 다 처리
      if (typeof config.headers.set === 'function') {
        config.headers.set('Authorization', '');
      } else {
        delete config.headers.Authorization;
      }
    } else if (token) {
      // 보호 경로면 토큰 부착
      if (typeof config.headers.set === 'function') {
        config.headers.set('Authorization', `Bearer ${token}`);
      } else {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  (error) => {
    hideLoading();
    return Promise.reject(error);
  }
);

// ===== [응답 인터셉터] =====
window.__alreadyRedirected = false;

api.interceptors.response.use(
  (res) => {
    hideLoading();
    return res;
  },
  (err) => {
    hideLoading();

    const status = err.response?.status;
    const reqUrl = err.config?.url;

    if ((status === 401 || status === 403) && !isPublicPath(reqUrl)) {
      if (!window.__alreadyRedirected) {
        window.__alreadyRedirected = true;
        alert('로그인 시간이 만료되었습니다. 다시 로그인해주세요.');
        setTimeout(() => {
          localStorage.removeItem('token');
          window.location.href = '/login';
        }, 100);
      }
      // Promise를 반환해서 catch 블록으로 에러가 전달되지 않게 함
      return new Promise(() => { });
    }

    return Promise.reject(err);
  }
);

export default api;