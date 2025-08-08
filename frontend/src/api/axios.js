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
api.interceptors.request.use(
  (config) => {
    // 로딩 시작
    showLoading();

    // 토큰 자동 실어주기
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    hideLoading();
    return Promise.reject(error);
  }
);

// ===== [응답 인터셉터] =====
api.interceptors.response.use(
  (res) => {
    hideLoading();
    return res;
  },
  (err) => {
    hideLoading();

    const status = err.response?.status;
    const data = err.response?.data;

// JWT 토큰 만료 처리
if (status === 401 || status === 403) {
  const msg =
    typeof data === 'string'
      ? data
      : data?.message || data?.error || '';

  // 메시지 내용 관계없이 처리 (안 보내줄 수도 있으니까)
  alert('세션이 만료되었거나 접근 권한이 없습니다. 다시 로그인해주세요!');

  setTimeout(() => {
    localStorage.removeItem('token');
    window.location.href = '/login'; // 또는 navigate('/login');
  }, 100);

  return Promise.reject(err);
}
  }
);

export default api;