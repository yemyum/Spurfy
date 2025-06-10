import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

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
api.interceptors.request.use((config) => {
  // 1. 로딩 시작
  showLoading();

  // 2. 토큰 자동 실어주기
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
}, (error) => {
  hideLoading();
  return Promise.reject(error);
});

// ===== [응답 인터셉터] =====
api.interceptors.response.use(
  (res) => {
    hideLoading();
    return res;
  },
  (err) => {
    hideLoading();

    if (err.response && err.response.status === 401) {
      console.warn('401 Unauthorized! 로그인 필요!');
      localStorage.removeItem('token');
      window.location.href = '/login'; // 로그인 경로 맞게 수정!
    }

    return Promise.reject(err);
  }
);

export default api;
