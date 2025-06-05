"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";
import { type User, authUtils } from "@/lib/auth";
import apiClient from "@/lib/api-client";
import { useAuthStore } from "@/store/auth";

// API_URL 제거

// 인증 컨텍스트 타입 정의
interface AuthContextType {
  user: User | null;
  isLoggedIn: boolean;
  isLoading: boolean;
  isAdmin: boolean;
  login: () => Promise<void>;
  logout: () => void;
}

// 인증 컨텍스트 생성
const AuthContext = createContext<AuthContextType>({
  user: null,
  isLoggedIn: false,
  isLoading: true,
  isAdmin: false,
  login: async () => {},
  logout: () => {},
});

// 인증 컨텍스트 훅
export const useAuth = () => useContext(AuthContext);

// 인증 프로바이더 컴포넌트
export function AuthProvider({ children }: { children: ReactNode }) {
  const zustandUser = useAuthStore((state) => state.user);
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const { accessToken, clearTokens } = useAuthStore();

  // zustand hydration 체크
  const [hydrated, setHydrated] = useState(false);
  useEffect(() => {
    setHydrated(true);
  }, []);

  // Zustand user가 바뀔 때마다 Context user도 동기화
  useEffect(() => {
    if (hydrated) {
      setUser(zustandUser);
      setIsLoading(false);
    }
  }, [hydrated, zustandUser]);

  async function fetchUserInfo(): Promise<User | null> {
    if (!accessToken) return null;
    try {
      const response = await apiClient.get<User>("/users", {
        headers: { access: accessToken },
      });
      if (!response.data) return null;
      const userData = response.data;
      if (!userData.role) userData.role = "user";
      return userData;
    } catch (e) {
      return null;
    }
  }

  useEffect(() => {
    async function init() {
      if (!hydrated) return;
      const userInfo = await fetchUserInfo();
      setUser(userInfo);
      setIsLoading(false);
    }
    init();
  }, [accessToken, hydrated]);

  useEffect(() => {
    console.log("[auth-provider] user 상태 변경:", user);
  }, [user]);

  const login = async () => {
    const userInfo = await fetchUserInfo();
    setUser(userInfo);
  };

  const logout = () => {
    authUtils.logout();
    setUser(null);
    clearTokens();
  };

  if (isLoading) {
    return null;
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoggedIn: user !== null,
        isLoading,
        isAdmin:
          user?.role?.toUpperCase() === "ADMIN" ||
          (user as any)?.data?.role?.toUpperCase() === "ADMIN",
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
