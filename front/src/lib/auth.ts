import apiClient from "./api-client";
import { useAuthStore } from "@/store/auth";

export interface RegisterData {
  loginId: string;
  password: string;
  name: string;
  email: string;
  phoneNumber: string;
  birthDay: string;
  role: string;
}

export interface LoginData {
  loginId: string;
  password: string;
}

export interface AuthResponse {
  message: string;
  user?: User;
  accessToken?: string;
  refreshToken?: string;
  data?: any;
}

export interface User {
  id: string;
  loginId: string;
  name: string;
  email: string;
  role: string;
  phoneNumber: string;
  birthDay?: string;
  birthday?: string;
}

/**
 * 현재 로그인한 사용자 정보 가져오기 (sessionStorage에서)
 * @returns {User | null} 사용자 정보 또는 null
 */
export const authUtils = {
  // 현재 사용자 정보 가져오기
  getCurrentUser: (): User | null => {
    if (typeof window === "undefined") return null;
    const userStr = sessionStorage.getItem("user");
    return userStr ? JSON.parse(userStr) : null;
  },

  // 사용자 정보 저장
  setCurrentUser: (user: User) => {
    sessionStorage.setItem("user", JSON.stringify(user));
  },

  // 사용자 정보 삭제
  clearCurrentUser: () => {
    sessionStorage.removeItem("user");
  },

  // 로그인 처리
  login: (user: User, accessToken: string, refreshToken: string) => {
    authUtils.setCurrentUser(user);
    useAuthStore.getState().setTokens(accessToken, refreshToken);
  },

  // 로그아웃 처리
  logout: () => {
    authUtils.clearCurrentUser();
    useAuthStore.getState().clearTokens();
  },
};

/**
 * 회원가입 요청
 * @param userData 회원가입 정보 (loginId, password, name, email, phoneNumber, birthDay, role)
 * @returns {Promise<AuthResponse>} 회원가입 결과 및 토큰
 * @example
 * await register({ loginId: "test", ... });
 */
export async function register(userData: RegisterData): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>(
    "/auth/register",
    userData
  );
  const { user, accessToken, refreshToken } = response.data;
  if (user && accessToken && refreshToken) {
    authUtils.login(user, accessToken, refreshToken);
  }
  return response.data;
}

/**
 * 로그인 요청
 * @param credentials 로그인 정보 (loginId, password)
 * @returns {Promise<AuthResponse>} 로그인 결과 및 토큰
 * @example
 * await login({ loginId: "test", password: "1234" });
 */
export async function login(credentials: LoginData): Promise<AuthResponse> {
  const response = await apiClient.post<AuthResponse>(
    "/auth/login",
    credentials
  );
  // 실제 토큰은 response.data.data.access, response.data.data.refresh에 있음
  const { user } = response.data;
  const accessToken = response.data.data?.access;
  const refreshToken = response.data.data?.refresh;

  if (user && accessToken && refreshToken) {
    authUtils.login(user, accessToken, refreshToken);
  }
  // refresh Next에서 저장해서 사용
  return {
    ...response.data,
    accessToken,
    refreshToken,
    user,
  };
}

/**
 * 현재 사용자가 인증(로그인)되어 있는지 확인
 * @returns {boolean} 인증 여부
 */
export function isAuthenticated(): boolean {
  return !!useAuthStore.getState().accessToken;
}

/**
 * 회원 탈퇴 요청
 * @returns {Promise<AuthResponse>} 탈퇴 결과
 * @example
 * await deleteUser();
 */
export async function deleteUser(): Promise<AuthResponse> {
  const response = await apiClient.delete<AuthResponse>("/users");
  return response.data;
}

/**
 * 유저 정보 수정
 * @param data 수정할 정보 (userId, name, email, phoneNumber, birthday)
 * @returns {Promise<any>} 수정 결과
 * @example
 * await updateUserInfo({ userId: "1", name: "홍길동", ... });
 */
export async function updateUserInfo(data: {
  userId: string;
  name: string;
  email: string;
  phoneNumber: string;
  birthday: string;
}) {
  const response = await apiClient.patch("/users", data);
  return response.data;
}
