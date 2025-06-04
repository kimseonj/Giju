"use client";

import { useState, useRef } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/components/common/auth-provider";
import { login } from "@/lib/auth";
import { useAuthStore } from "@/store/auth";
import apiClient from "@/lib/api-client";
import type { AuthState, User } from "@/store/auth";
import axios from "axios";
import SocialLoginButtons from "@/components/common/social-login-buttons";
import Image from "next/image";

const KAKAO_OAUTH_URL =
  process.env.NEXT_PUBLIC_API_URL + "/oauth2/authorization/kakao";

export default function LoginForm() {
  const router = useRouter();
  const { login: authLogin } = useAuth();
  const setTokens = useAuthStore((state: AuthState) => state.setTokens);
  const setUser = useAuthStore((state: any) => state.setUser);

  const [formData, setFormData] = useState({
    loginId: "",
    password: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [tab, setTab] = useState("user");

  const formRef = useRef<HTMLFormElement>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.loginId.trim()) {
      newErrors.loginId = "아이디를 입력해주세요.";
    }

    if (!formData.password) {
      newErrors.password = "비밀번호를 입력해주세요.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      const response = await login(formData);
      console.log("로그인 응답 전체:", response);
      console.log("message:", response.message);
      console.log("accessToken:", response.accessToken);
      console.log("refreshToken:", response.refreshToken);
      if (
        response.message === "로그인 성공" &&
        response.accessToken &&
        response.refreshToken
      ) {
        setTokens(response.accessToken, response.refreshToken);
        console.log("토큰값", response.accessToken);
        console.log("로그인 후 토큰 저장, 유저 정보 요청 시작");
        try {
          const userResponse = await apiClient.get("/users", {
            headers: {
              access: response.accessToken,
            },
          });
          console.log("유저 정보 요청 결과:", userResponse);
          if ((userResponse as any).data) {
            setUser((userResponse as any).data.data);
            console.log("setUser 실행, user:", (userResponse as any).data.data);
          }
        } catch (e) {
          console.log("유저 정보 요청 실패:", e);
        }
        router.push("/");
      } else {
        alert(response.message || "로그인에 실패했습니다.");
      }
    } catch (error: any) {
      console.log("로그인 오류 상세:", {
        error: error,
        message: error?.message,
        status: error?.status,
        timeStamp: error?.timeStamp,
      });
      const errorMessage =
        error?.message || "로그인에 실패했습니다. 다시 시도해주세요.";
      alert(errorMessage);
    }
  };

  return (
    <div className="bg-gray-100 flex flex-col items-center justify-center">
      <div className="flex flex-col items-center justify-center w-full min-h-[780px]">
        <div className="w-full flex flex-col items-center">
          {/* 로고 */}
          <div className="flex justify-center mb-6">
            <img src="/dark-logo.svg" alt="기주 로고" className="h-16 w-auto" />
          </div>
          {/* 안내문구 */}
          <div className="text-center font-pretendard text-[16px] mb-8 font-light text-sub-dark">
            전통주 이커머스 사이트
            <br />
            '기주'에 오신것을 환영합니다.
          </div>
          {/* 입력폼 카드: 탭+입력란만 */}
          <div className="bg-white border-t border-b border-main rounded-none pt-0 pb-0 mt-0 w-[580px] h-[240px] mx-auto">
            {/* 탭: 카드 내부 */}
            <div className="flex border-b border-[#BDBDBD] py-3 mb-0 w-[580px] mx-auto">
              <button
                type="button"
                className={`flex-1 py-3 text-center text-[21px] font-pretendard font-light border-none bg-transparent focus:outline-none cursor-pointer ${
                  tab === "admin" ? "text-main" : "text-[#BDBDBD]"
                }`}
                onClick={() => setTab("admin")}
              >
                관리자 로그인
              </button>
              <button
                type="button"
                className={`flex-1 py-3 text-center text-[21px] font-pretendard font-light border-none bg-transparent focus:outline-none cursor-pointer ${
                  tab === "user" ? "text-main" : "text-[#BDBDBD]"
                }`}
                onClick={() => setTab("user")}
              >
                일반 사용자 로그인
              </button>
            </div>
            {/* 입력폼만 form으로 감쌈 */}
            <form ref={formRef} className="px-8 h-32" onSubmit={handleSubmit}>
              <input
                id="loginId"
                name="loginId"
                type="text"
                value={formData.loginId}
                onChange={handleChange}
                placeholder="아이디"
                className="w-full bg-transparent py-6 px-0 text-[18px] text-main focus:outline-none placeholder-sub-dark border-none"
              />
              {/* 아이디/비밀번호 사이에만 보더 */}
              <div className="border-b border-[#BDBDBD] w-full" />
              <input
                id="password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="비밀번호"
                className="w-full bg-transparent py-6 px-0 text-[18px] text-main focus:outline-none placeholder-sub-dark border-none"
              />
              {/* 숨겨진 submit 버튼 */}
              <button type="submit" style={{ display: "none" }} tabIndex={-1} />
            </form>
          </div>
          {/* 카드 밖(아래)에 체크박스/찾기/로그인/소셜로그인 */}
          <div className="w-[644px] mx-auto flex flex-col items-center px-8">
            <div className="flex items-center justify-between mt-2 mb-4 w-full">
              <label className="flex items-center text-main text-[14px]">
                <input type="checkbox" className="mr-1 accent-main size-4" />
                아이디 저장
              </label>
              <div className="font-pretendard font-light text-[14px] text-[#B18B6C]">
                <Link href="/forgot-id" className="hover:underline">
                  아이디 찾기
                </Link>{" "}
                |{" "}
                <Link href="/forgot-password" className="hover:underline">
                  비밀번호 찾기
                </Link>
              </div>
            </div>
            <button
              type="button"
              onClick={() => formRef.current?.requestSubmit()}
              className="w-full h-[68px] relative text-white text-[18px] font-bold rounded-none mb-4 mt-2 font-pretendard tracking-wider border-none overflow-hidden flex items-center justify-center cursor-pointer"
            >
              {/* 배경 이미지 */}
              <Image
                src="/search-bg.svg"
                alt="로그인 버튼 배경"
                fill
                className="absolute left-0 top-0 w-full h-full z-0 object-contain"
              />
              {/* 텍스트 */}
              <span className="font-noh text-[21pt] font-light relative z-10">
                로그인
              </span>
            </button>
            {/* 소셜 로그인 버튼 */}
            <div className="flex gap-2 mb-2 w-full">
              <button
                type="button"
                onClick={() => {}}
                className="flex-1 h-[44px] bg-[#03C75A] text-white font-bold rounded-none flex items-center justify-center text-[15px] hover:bg-[#02B350] transition-colors cursor-pointer"
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 18 18"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                  className="mr-2"
                >
                  <path
                    d="M12.1616 9.57941L5.50756 0H0V18H5.83842V8.42059L12.4925 18H18V0H12.1616V9.57941Z"
                    fill="white"
                  />
                </svg>
                네이버 로그인
              </button>
              <button
                type="button"
                onClick={() => {
                  window.location.href = KAKAO_OAUTH_URL;
                }}
                className="flex-1 h-[44px] bg-[#FEE500] text-[#3C1E1E] font-bold rounded-none flex items-center justify-center text-[15px] hover:bg-[#FDDC3F] transition-colors cursor-pointer"
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 18 18"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                  className="mr-2"
                >
                  <path
                    fillRule="evenodd"
                    clipRule="evenodd"
                    d="M9 0.5C4.02944 0.5 0 3.69844 0 7.68645C0 10.1734 1.55542 12.3607 3.93131 13.5659L2.93275 17.1151C2.84637 17.4087 3.19288 17.6406 3.44362 17.4563L7.60769 14.6479C8.06709 14.7037 8.53709 14.7324 9 14.7324C13.9706 14.7324 18 11.5745 18 7.58645C18 3.59844 13.9706 0.5 9 0.5Z"
                    fill="#3C1E1E"
                  />
                </svg>
                카카오 로그인
              </button>
            </div>
            {/* 회원가입 안내 */}
            <div className="pr-86 mt-4 font-pretendard font-light text-[14px] text-sub-dark">
              아직 기주의 회원이 아니신가요?{" "}
              <Link
                href="/signup"
                className="text-[#B18B6C] font-pretendard font-light ml-1 hover:underline"
              >
                회원가입
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// 입력 필드 공통 컴포넌트
function InputField({
  label,
  id,
  type,
  value,
  onChange,
  error,
  inputClassName = "",
}: {
  label: string;
  id: string;
  type: string;
  value: string;
  error?: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  inputClassName?: string;
}) {
  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-gray-700">
        {label}
      </label>
      <div className="mt-1">
        <input
          id={id}
          name={id}
          type={type}
          value={value}
          onChange={onChange}
          className={`appearance-none block w-full px-3 ${inputClassName} ${
            error ? "border-red-300" : "border-gray-300"
          }`}
        />
        {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
      </div>
    </div>
  );
}
