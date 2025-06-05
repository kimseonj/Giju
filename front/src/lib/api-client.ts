import axios from "axios";
import { useAuthStore } from "@/store/auth";

const baseURL = process.env.NEXT_PUBLIC_API_URL + "/api";

const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * Request 인터셉터
 * - 요청 시 accessToken이 있으면 헤더에 추가
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().accessToken;
    if (token && config.headers) {
      config.headers["access"] = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response 인터셉터
 * - 401(토큰 만료) 발생 시 자동으로 토큰 재발급 시도
 * - 재발급 성공 시 원래 요청을 새로운 토큰으로 재시도
 * - 재발급 실패 시 토큰 삭제 및 로그인 페이지로 이동
 */
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 토큰 만료로 인한 401 에러이고, 재시도하지 않은 요청인 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // 리프레시 토큰으로 새로운 액세스 토큰 발급 (쿠키 기반, body/params 없이 요청)
        const response = await axios.post("/auth/refresh", null, {
          withCredentials: true,
        });

        // 스웨거 예시: data: { access: '...', refresh: '...' }
        const accessToken = (response.data as any).data?.access;
        const refreshToken = (response.data as any).data?.refresh;
        useAuthStore.getState().setTokens(accessToken, refreshToken ?? "");

        // 새로운 토큰으로 원래 요청 재시도
        originalRequest.headers.access = accessToken;
        return apiClient(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().clearTokens();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
