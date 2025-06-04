"use client";

import { useState, useEffect } from "react";
import {
  Search,
  Filter,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown,
  Eye,
  Ban,
  CheckCircle,
} from "lucide-react";
import { getUsers } from "@/lib/user";

// 유저 타입 정의
interface User {
  userId: string;
  loginId: string;
  name: string;
  email: string;
  phoneNumber: string;
  birthday: string;
  createdAt: string;
  role: string;
}

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedStatus, setSelectedStatus] = useState<string | null>(null);
  const pageSize = 10;

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response: any = await getUsers(currentPage, pageSize);
        // 실제 API 응답 구조에 맞게 파싱 (방어코드 추가)
        const userData = (response?.data?.content ?? []).map((item: any) => ({
          userId: item.userId,
          loginId: item.loginId,
          name: item.name,
          email: item.email,
          phoneNumber: item.phoneNumber,
          birthday: item.birthday,
          createdAt: item.createdAt,
          role: item.role,
        }));
        setUsers(userData);
        setTotalPages(response?.data?.totalPages ?? 0);
      } catch (error) {
        setUsers([]); // 에러 시에도 빈 배열로
        setTotalPages(0);
        console.error("유저 데이터를 불러오는데 실패했습니다:", error);
      }
    };
    fetchUsers();
  }, [currentPage]);

  // 필터링된 유저 목록
  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.phoneNumber.includes(searchTerm);
    return matchesSearch;
  });

  return (
    <div className="w-full flex flex-col items-center bg-white min-h-screen">
      <div className="w-full max-w-[1100px] mt-15">
        <div className="mb-0">
          <h1 className="text-[21px] font-pretendard font-medium text-main mb-0 text-left">
            회원관리
          </h1>
        </div>
        {/* 검색 및 필터 */}
        <div className="flex items-center mb-4">
          <div className="relative flex-1 max-w-[600px] h-[80px]">
            {/* 검색창 SVG 배경 */}
            <img
              src="/user-search.svg"
              alt="검색창"
              className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[80px] pointer-events-none select-none"
              draggable={false}
            />
            {/* 검색 아이콘은 기존대로 */}
            <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-[#B0B8C1] w-6 h-6 z-10" />
            {/* input은 SVG 내부에 맞게 */}
            <input
              type="text"
              placeholder="이름, 이메일, 전화번호 검색"
              className="absolute left-0 top-0 w-full h-full bg-transparent border-none outline-none pl-14 pr-[140px] text-[17px] font-pretendard placeholder:font-light z-10"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ minWidth: 0 }}
            />
            {/* 카테고리 필터 버튼을 검색창 내부 오른쪽에 배치 */}
            <button
              className="absolute right-1 top-1/2 -translate-y-1/2 px-5 h-[40px] min-w-[80px] bg-transparent font-pretendard font-light text-[15px] text-[#B18B6C] flex items-center justify-center z-20"
              style={{ boxShadow: "none" }}
              disabled
            >
              카테고리 필터
            </button>
          </div>
        </div>
        {/* 회원 테이블 */}
        <div className="bg-white overflow-hidden shadow-none border-t-3 border-main">
          <table className="w-full text-center">
            <thead>
              <tr className="text-main font-pretendard text-[18px] font-light bg-white border-b border-[#E5EAF2]">
                <th className="py-3 px-2 font-medium">ID</th>
                <th className="py-3 px-2 font-medium">이름</th>
                <th className="py-3 px-2 font-medium">이메일</th>
                <th className="py-3 px-2 font-medium">전화번호</th>
                <th className="py-3 px-2 font-medium">가입일</th>
                <th className="py-3 px-2 font-medium">주문수</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((user, idx) => {
                const isLast = idx === filteredUsers.length - 1;
                return (
                  <tr
                    key={user.userId}
                    className={
                      "bg-white hover:bg-[#F7F7F7] " +
                      (isLast
                        ? "border-b-2 border-sub-dark"
                        : "border-b border-[#E5EAF2]")
                    }
                  >
                    <td className="py-3 px-2 text-[15px] font-pretendard text-sub-dark font-light">
                      {user.loginId}
                    </td>
                    <td className="py-3 px-2 text-[15px] font-pretendard text-main">
                      {user.name}
                    </td>
                    <td className="py-3 px-2 text-[15px] font-pretendard text-sub-dark font-light">
                      {user.email}
                    </td>
                    <td className="py-3 px-2 text-[15px] font-pretendard text-sub-dark font-light">
                      {user.phoneNumber}
                    </td>
                    <td className="py-3 px-2 text-[15px] font-pretendard text-sub-dark font-light">
                      {user.createdAt ? user.createdAt.slice(0, 10) : ""}
                    </td>
                    <td className="py-3 px-2 text-[15px] font-pretendard text-sub-dark font-light">
                      2건
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          {/* 하단 요약/페이지네이션 */}
          <div className="flex items-center justify-between px-2 py-4 bg-white">
            <div className="text-[15px] text-[#222] text-left">
              총 <span className="font-bold">{filteredUsers.length}</span>명의
              회원
            </div>
            <div className="flex items-center gap-2">
              <button
                className="w-7 h-7 flex items-center justify-center rounded bg-white text-[#222] disabled:opacity-40"
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={currentPage === 1}
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <span className="text-[15px] px-2">page{currentPage}</span>
              <button
                className="w-7 h-7 flex items-center justify-center rounded bg-white text-[#222] disabled:opacity-40"
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={currentPage === totalPages}
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
