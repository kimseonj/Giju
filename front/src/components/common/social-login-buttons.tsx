"use client";

interface SocialLoginButtonsProps {
  onSocialLogin: (provider: string) => void;
  type: "login" | "register";
}

export default function SocialLoginButtons({
  onSocialLogin,
  type,
}: SocialLoginButtonsProps) {
  const actionText = type === "login" ? "로그인" : "회원가입";

  return (
    <div>
      <div className="relative">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-gray-300" />
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="px-2 bg-white text-gray-500">
            소셜 계정으로 {actionText}
          </span>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-4">
        {/* 카카오 버튼 */}
        <button
          onClick={() => onSocialLogin("kakao")}
          className="flex-1 h-[44px] bg-[#FEE500] text-[#3C1E1E] font-bold rounded-none flex items-center justify-center text-[15px] hover:bg-[#FDDC3F] transition-colors"
        >
          <div className="flex items-center">
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
            <span className="text-[#3C1E1E] text-[15px] font-bold">
              카카오 로그인
            </span>
          </div>
        </button>

        {/* 네이버 버튼 */}
        <button
          onClick={() => onSocialLogin("naver")}
          className="flex-1 h-[44px] bg-[#03C75A] text-white font-bold rounded-none flex items-center justify-center text-[15px] hover:bg-[#02B350] transition-colors"
        >
          <div className="flex items-center">
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
            <span className="text-white text-[15px] font-bold">
              네이버 로그인
            </span>
          </div>
        </button>
      </div>
    </div>
  );
}
