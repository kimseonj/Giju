"use client";

import { Fragment, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/common/auth-provider";
import { deleteUser, updateUserInfo } from "@/lib/auth";
import apiClient from "@/lib/api-client";
import type { User } from "@/lib/auth";
import { useAuthStore } from "@/store/auth";
import React from "react";
import { getOrderHistory } from "@/lib/order";
import Image from "next/image";

export default function MyPage() {
  const { isLoggedIn } = useAuth();
  const router = useRouter();
  const [redirecting, setRedirecting] = useState(false);
  const [orders, setOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isLoggedIn === false && !redirecting) {
      setRedirecting(true);
      router.replace("/login");
    }
  }, [isLoggedIn, redirecting, router]);

  useEffect(() => {
    if (isLoggedIn !== true) return;
    async function fetchOrders() {
      try {
        setLoading(true);
        const res = await getOrderHistory();
        const result = res as any;
        setOrders(result.data || result);
      } catch (e: any) {
        setError(e?.message || "주문 내역을 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    }
    fetchOrders();
  }, [isLoggedIn]);

  if (typeof isLoggedIn === "undefined") {
    return <div />;
  }
  if (isLoggedIn === false && redirecting) {
    return <div />;
  }
  if (isLoggedIn !== true) {
    return null;
  }

  return (
    <main className="flex-1 py-10 px-12">
      {/* 주문목록 상단 */}
      <div className="flex items-center justify-between mb-2">
        <div className="font-pretendard text-[21px] font-extrabold text-main">
          주문목록
        </div>
        <div className="flex gap-4 text-[#888] text-sm">
          <button className="hover:text-main">최근 6개월</button>
          <button className="hover:text-main">2025</button>
          <button className="hover:text-main">2024</button>
          <button className="hover:text-main">2023</button>
          <button className="hover:text-main">2022</button>
          <button className="hover:text-main">2021</button>
          <button className="hover:text-main">이전년도 보기</button>
        </div>
      </div>
      {/* 검색창 */}
      <form
        className="relative flex items-center w-[328px] h-[38px] mb-6"
        style={{ maxWidth: 328 }}
      >
        {/* 검색창 배경 SVG */}
        <Image
          src="/search-bg.svg"
          alt="검색창 배경"
          fill
          className="absolute left-0 top-0 w-full h-full z-0"
        />
        <input
          type="text"
          placeholder="주문상품 검색"
          className="pl-4 pr-10 bg-transparent border-none w-full h-full relative z-10 focus:outline-none font-noh text-[18px] text-white placeholder:text-sub-light search-placeholder-fix"
          style={{
            background: "transparent",
            height: "38px",
            lineHeight: "38px",
            paddingTop: 0,
            paddingBottom: 0,
          }}
        />
        {/* 돋보기 아이콘 */}
        <button
          type="submit"
          className="absolute right-2 top-1/2 -translate-y-1/2 z-10"
        >
          <Image src="/search-icon.svg" alt="검색" width={22} height={22} />
        </button>
      </form>
      {/* 주문 리스트 */}
      <div className="space-y-10">
        {loading && (
          <div className="text-center py-10 text-gray-400">
            주문 내역을 불러오는 중...
          </div>
        )}
        {error && <div className="text-center py-10 text-red-500">{error}</div>}
        {!loading && !error && orders.length === 0 && (
          <div className="text-center py-10 text-gray-400">
            주문 내역이 없습니다.
          </div>
        )}
        {!loading &&
          !error &&
          orders.map((order: any) => (
            <div key={order.orderId} className="border-b py-6">
              <div className="flex justify-between items-center mb-2">
                <div className="font-bold text-main">
                  주문번호 {order.orderId} |{" "}
                  {order.orderedAt ? order.orderedAt.slice(0, 10) : "-"}{" "}
                  {order.orderedAt ? order.orderedAt.slice(11, 16) : ""}
                </div>
                <div className="text-sm text-[#888]">{order.orderStatus}</div>
              </div>
              <div className="mb-2 text-sm text-[#444]">
                결제수단: {order.paymentMethod} | 총 결제금액:{" "}
                {order.totalAmount?.toLocaleString()}원
              </div>
              <ul className="mb-2">
                {order.items &&
                  order.items.map((item: any, idx: number) => (
                    <li key={idx} className="flex justify-between text-[15px]">
                      <span>
                        {item.drinkName}{" "}
                        <span className="text-[#888]">x{item.quantity}</span>
                      </span>
                      <span>{item.totalPrice?.toLocaleString()}원</span>
                    </li>
                  ))}
              </ul>
              <div className="text-right">
                <button className="text-main underline text-sm">
                  주문 상세보기
                </button>
              </div>
            </div>
          ))}
      </div>
    </main>
  );
}
