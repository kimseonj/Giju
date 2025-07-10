"use client";
import { useAuth } from "@/components/common/auth-provider";
import { useRouter } from "next/navigation";

export default function ProfilePage() {
  const { user, isLoggedIn, isLoading } = useAuth();
  const router = useRouter();

  if (isLoading) return null;
  if (!isLoggedIn) {
    router.replace("/login");
    return null;
  }

  // 실제 유저 정보는 user.data에 있음
  const userData = (user as any)?.data || {};

  let birth = "-";
  if (userData.birthday) {
    const [y, m, d] = userData.birthday.split("-");
    birth = `${y}-${m}-${d}`;
  }

  return (
    <div className="w-full flex flex-col items-center min-h-screen">
      <div className="w-full max-w-[1000px]">
        <div className="mb-4 flex justify-between items-end">
          <h1 className="font-jj text-[36px] font-extrabold text-main">
            기본정보
          </h1>
          {/* <span className="text-[13px] text-[#B18B6C]">
            * 표시는 필수 입력항목
          </span> */}
        </div>
        <div className="border-b-4 border-main"></div>
        <div className="bg-white overflow-hidden">
          <table className="w-full">
            <tbody>
              <tr className="border-b border-[#E5EAF2]">
                <th className="py-4 px-6 text-left w-[160px] font-medium text-main border-r border-[#E5EAF2]">
                  아이디
                </th>
                <td className="py-4 px-6">{userData.loginId || "-"}</td>
              </tr>
              <tr>
                <th className="py-4 px-6 text-left w-[160px] font-medium border-r border-b border-sub-light text-main">
                  이름
                </th>
                <td className="py-4 px-6">{userData.name || "-"}</td>
              </tr>
              <tr>
                <th className="py-4 px-6 text-left w-[160px] font-medium border-b border-r border-sub-light text-main">
                  배송지
                </th>
                <td className="py-0 px-4 border-b border-t border-sub-light h-[140px] align-middle">
                  <div className="flex flex-col h-full">
                    {/* 우편번호 줄 */}
                    <div className="flex items-center gap-2 border-b border-[#E5EAF2] flex-1 min-h-0">
                      <span className="font-medium text-main">12345</span>
                    </div>
                    {/* 주소 줄 */}
                    <div className="flex items-center border-b border-[#E5EAF2] text-[#22313F] flex-1 min-h-0">
                      광주광역시 00구 00로 00번길 12-34
                    </div>
                    {/* 상세주소 줄 */}
                    <div className="flex items-center text-[#B0B8C1] flex-1 min-h-0">
                      상세주소 입력
                    </div>
                  </div>
                </td>
              </tr>
              <tr className="border-b border-[#E5EAF2]">
                <th className="py-4 px-6 text-left w-[160px] font-medium text-main border-r border-[#E5EAF2]">
                  이메일
                </th>
                <td className="py-4 px-6">{userData.email || "-"}</td>
              </tr>
              <tr className="border-b border-[#E5EAF2]">
                <th className="py-4 px-6 text-left w-[160px] font-medium text-main border-r border-[#E5EAF2]">
                  일반전화
                </th>
                <td className="py-4 px-6">{userData.phoneNumber || "-"}</td>
              </tr>
              <tr className="border-b-3 border-sub-dark">
                <th className="py-4 px-6 text-left w-[160px] font-medium text-main border-r border-[#E5EAF2]">
                  생년월일
                </th>
                <td className="py-4 px-6">{birth}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
