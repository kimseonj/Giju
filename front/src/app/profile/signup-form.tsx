"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/components/common/auth-provider";
import SocialLoginButtons from "@/components/common/social-login-buttons";
import { register } from "@/lib/auth";
import { useAuthStore } from "@/store/auth";
import type { AuthState } from "@/store/auth";

export default function RegisterPage() {
  const router = useRouter();
  const { login: authLogin } = useAuth();
  const setTokens = useAuthStore((state: AuthState) => state.setTokens);

  const [formData, setFormData] = useState({
    loginId: "",
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    phoneNumber: "",
    birthDay: "",
    agreeTerms: false,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    // 아이디 검증
    if (!formData.loginId.trim()) {
      newErrors.loginId = "아이디를 입력해주세요.";
    }

    // 이름 검증
    if (!formData.name.trim()) {
      newErrors.name = "이름을 입력해주세요.";
    }

    // 이메일 검증
    if (!formData.email.trim()) {
      newErrors.email = "이메일을 입력해주세요.";
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = "유효한 이메일 주소를 입력해주세요.";
    }

    // 비밀번호 검증
    if (!formData.password) {
      newErrors.password = "비밀번호를 입력해주세요.";
    } else if (formData.password.length < 6) {
      newErrors.password = "비밀번호는 최소 6자 이상이어야 합니다.";
    }

    // 비밀번호 확인 검증
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "비밀번호가 일치하지 않습니다.";
    }

    // 전화번호 검증
    if (!formData.phoneNumber.trim()) {
      newErrors.phoneNumber = "전화번호를 입력해주세요.";
    } else if (
      !/^01([0|1|6|7|8|9])-?([0-9]{3,4})-?([0-9]{4})$/.test(
        formData.phoneNumber
      )
    ) {
      newErrors.phoneNumber = "유효한 전화번호를 입력해주세요.";
    }

    // 생년월일 검증
    if (!formData.birthDay) {
      newErrors.birthDay = "생년월일을 입력해주세요.";
    } else {
      const birthDate = new Date(formData.birthDay);
      const today = new Date();
      if (birthDate > today) {
        newErrors.birthDay = "생년월일은 오늘 이전이어야 합니다.";
      }
    }

    // 약관 동의 검증
    if (!formData.agreeTerms) {
      newErrors.agreeTerms = "이용약관에 동의해주세요.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) return;

    try {
      const response = await register({
        loginId: formData.loginId,
        password: formData.password,
        name: formData.name,
        email: formData.email,
        phoneNumber: formData.phoneNumber,
        birthDay: formData.birthDay.replace(/-/g, ""), // YYYY-MM-DD 형식을 YYYYMMDD로 변환
        role: "user", // 기본적으로 일반 사용자 권한 부여
      });

      if (
        response.message === "success" &&
        response.accessToken &&
        response.refreshToken
      ) {
        setTokens(response.accessToken, response.refreshToken);
        await authLogin();
        router.push("/");
      } else {
        alert(response.message || "회원가입에 실패했습니다.");
      }
    } catch (error: any) {
      console.error("회원가입 오류:", error);
      alert(error.message || "회원가입에 실패했습니다.");
    }
  };

  // 소셜 회원가입 처리 (실제로는 각 플랫폼 API 연동 필요)
  // const handleSocialRegister = (provider: string) => {
  //   // 실제 구현에서는 각 소셜 로그인 API로 리다이렉트
  //   console.log(`${provider} 회원가입 시도`);

  //   // 테스트용: 소셜 회원가입 성공 가정
  //   if (provider === "kakao") {
  //     login({
  //       id: 3,
  //       name: "카카오 사용자",
  //       email: "kakao_user@example.com",
  //       role: "user",
  //     });
  //   } else if (provider === "naver") {
  //     login({
  //       id: 4,
  //       name: "네이버 사용자",
  //       email: "naver_user@example.com",
  //       role: "user",
  //     });
  //   }

  //   router.push("/");
  // };

  return (
    <div className="bg-gray-50 py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <Link href="/" className="flex items-center justify-center">
          <span className="text-3xl font-bold">기주</span>
          <span className="text-sm ml-1 text-gray-500">[寄酒]</span>
        </Link>
        <h2 className="mt-6 text-center text-2xl font-bold text-gray-900">
          회원가입
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {/* 아이디 */}
            <InputField
              label="아이디"
              id="loginId"
              type="text"
              value={formData.loginId}
              onChange={handleChange}
              error={errors.loginId}
            />

            {/* 이름 */}
            <InputField
              label="이름"
              id="name"
              type="text"
              value={formData.name}
              onChange={handleChange}
              error={errors.name}
            />

            {/* 이메일 */}
            <InputField
              label="이메일"
              id="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              error={errors.email}
            />

            {/* 비밀번호 */}
            <InputField
              label="비밀번호"
              id="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              error={errors.password}
            />

            {/* 비밀번호 확인 */}
            <InputField
              label="비밀번호 확인"
              id="confirmPassword"
              type="password"
              value={formData.confirmPassword}
              onChange={handleChange}
              error={errors.confirmPassword}
            />

            {/* 전화번호 */}
            <InputField
              label="전화번호"
              id="phoneNumber"
              type="text"
              value={formData.phoneNumber}
              onChange={handleChange}
              error={errors.phoneNumber}
            />

            {/* 생년월일 */}
            <InputField
              label="생년월일"
              id="birthDay"
              type="date"
              value={formData.birthDay}
              onChange={handleChange}
              error={errors.birthDay}
              max={new Date().toISOString().split("T")[0]} // 오늘 날짜를 최대값으로 설정
            />

            {/* 약관 동의 */}
            <div className="flex items-start">
              <input
                id="agreeTerms"
                name="agreeTerms"
                type="checkbox"
                checked={formData.agreeTerms}
                onChange={handleChange}
                className="h-4 w-4 text-orange-600 border-gray-300 rounded focus:ring-orange-500"
              />
              <div className="ml-3 text-sm">
                <label
                  htmlFor="agreeTerms"
                  className="font-medium text-gray-700"
                >
                  이용약관 및 개인정보처리방침에 동의합니다.
                </label>
                <p className="text-gray-500">
                  <Link
                    href="/terms"
                    className="text-orange-600 hover:text-orange-500"
                  >
                    이용약관
                  </Link>{" "}
                  및{" "}
                  <Link
                    href="/privacy"
                    className="text-orange-600 hover:text-orange-500"
                  >
                    개인정보처리방침
                  </Link>
                  을 읽고 이해하였습니다.
                </p>
                {errors.agreeTerms && (
                  <p className="mt-1 text-sm text-red-600">
                    {errors.agreeTerms}
                  </p>
                )}
              </div>
            </div>

            <button
              type="submit"
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-main hover:bg-primary-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
            >
              회원가입
            </button>
          </form>

          {/* <div className="mt-6">
            <SocialLoginButtons
              onSocialLogin={handleSocialRegister}
              type="register"
            />
          </div> */}

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              이미 계정이 있으신가요?{" "}
              <Link
                href="/login"
                className="font-medium text-orange-600 hover:text-orange-500"
              >
                로그인
              </Link>
            </p>
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
  max,
}: {
  label: string;
  id: string;
  type: string;
  value: string;
  error?: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  max?: string;
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
          className={`appearance-none block w-full px-3 py-2 border ${
            error ? "border-red-300" : "border-gray-300"
          } rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-orange-500 focus:border-orange-500`}
          max={max}
        />
        {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
      </div>
    </div>
  );
}
