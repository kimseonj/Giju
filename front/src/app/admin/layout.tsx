"use client";

import type React from "react";
import { useState, useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  BarChart3,
  Package,
  Users,
  MessageSquare,
  Star,
  Settings,
  LogOut,
  Menu,
  X,
} from "lucide-react";
import { useAuth } from "@/components/common/auth-provider";

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const router = useRouter();
  const { logout, isAdmin, isLoggedIn, isLoading } = useAuth();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  useEffect(() => {
    if (!isLoading) {
      if (!isLoggedIn) {
        router.replace("/login");
      } else if (!isAdmin) {
        router.replace("/");
      }
    }
  }, [isLoggedIn, isAdmin, isLoading, router]);

  if (isLoading || !isLoggedIn || !isAdmin) return null;

  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
  };

  const handleLogout = () => {
    logout();
    router.push("/");
  };

  const navItems = [
    {
      href: "/admin",
      label: "대시보드",
      icon: <BarChart3 className="w-5 h-5" />,
    },
    {
      href: "/admin/products",
      label: "상품 관리",
      icon: <Package className="w-5 h-5" />,
    },
    {
      href: "/admin/users",
      label: "회원 관리",
      icon: <Users className="w-5 h-5" />,
    },
    {
      href: "/admin/qna",
      label: "Q&A 관리",
      icon: <MessageSquare className="w-5 h-5" />,
    },
    {
      href: "/admin/reviews",
      label: "리뷰 관리",
      icon: <Star className="w-5 h-5" />,
    },
    {
      href: "/admin/settings",
      label: "설정",
      icon: <Settings className="w-5 h-5" />,
    },
  ];

  return (
    <div className="min-h-screen bg-white font-pretendard">
      {/* 모바일 헤더 */}
      <div className="md:hidden bg-white border-b p-4 flex items-center justify-between">
        <div className="flex items-center">
          <button onClick={toggleSidebar} className="mr-2">
            {isSidebarOpen ? (
              <X className="w-6 h-6" />
            ) : (
              <Menu className="w-6 h-6" />
            )}
          </button>
          <h1 className="text-xl font-bold">기주 관리자</h1>
        </div>
      </div>

      <div className="flex">
        {/* 사이드바 */}
        <aside
          className={`fixed top-0 left-0 z-40 h-screen transition-transform ${
            isSidebarOpen ? "translate-x-0" : "-translate-x-full"
          } md:translate-x-0 bg-gray-100 border-r border-[#E5E5E5] w-[260px] md:sticky md:top-0`}
        >
          <div className="flex flex-col h-full">
            {/* 상단 관리자 정보 */}
            <div className="pt-12 pb-8 px-8 border-b border-[#E5E5E5]">
              <div className="font-pretendard text-[21px] font-extrabold text-main mb-1">
                홍길동 관리자
              </div>
              <div className="font-pretendard text-[14px] text-sub-dark">
                전통주 쇼핑몰 관리
              </div>
            </div>

            {/* 메뉴 그룹 */}
            <nav className="flex-1 px-0 py-8 space-y-0">
              <div className="text-sub-dark text-[14px] font-semibold mb-3 pl-10 tracking-widest">
                관리 메뉴
              </div>
              <ul className="space-y-1">
                {navItems.map((item) => {
                  const isActive = pathname === item.href;
                  return (
                    <li key={item.href}>
                      <Link
                        href={item.href}
                        className={`flex items-center gap-3 px-10 py-3 rounded-none relative transition-colors text-[21px] font-pretendard
                          ${
                            isActive
                              ? "font-extrabold text-main bg-[#FFF6ED]"
                              : "font-light text-[#222] hover:bg-[#F7F7F7]"
                          }
                        `}
                        style={
                          isActive
                            ? {
                                borderLeft: "4px solid #FF9100",
                                background: "#FFF6ED",
                              }
                            : { borderLeft: "4px solid transparent" }
                        }
                      >
                        {item.icon}
                        <span>{item.label}</span>
                      </Link>
                    </li>
                  );
                })}
              </ul>
            </nav>

            {/* 하단: 쇼핑몰로 이동, 로그아웃 */}
            <div className="px-8 py-8 border-t border-[#E5E5E5] mt-auto">
              <Link
                href="/"
                className="block text-[#B18B6C] font-pretendard text-[14px] px-3 text-left font-light mb-4 hover:underline transition-all"
              >
                쇼핑몰로 이동
              </Link>
              <button
                onClick={handleLogout}
                className="block text-[#888] hover:underline rounded px-3 py-2 w-full text-left font-pretendard text-[14px] font-light cursor-pointer"
              >
                로그아웃
              </button>
            </div>
          </div>
        </aside>

        {/* 메인 콘텐츠 */}
        <main className="flex-1">{children}</main>
      </div>
    </div>
  );
}
