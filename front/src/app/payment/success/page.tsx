"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { paymentSuccess } from "@/lib/payment";
import Image from "next/image";
import Link from "next/link";

export default function PaymentSuccessPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const processPayment = async () => {
      try {
        const paymentKey = searchParams.get("paymentKey");
        const orderId = searchParams.get("orderId");
        const amount = searchParams.get("amount");

        console.log("[TOSS] 결제 성공 쿼리스트링", {
          paymentKey,
          orderId,
          amount,
        });

        if (!paymentKey || !orderId || !amount) {
          throw new Error("필수 결제 정보가 누락되었습니다.");
        }

        console.log("[TOSS] paymentSuccess 호출 시작", {
          paymentKey,
          orderId,
          amount,
        });
        await paymentSuccess(paymentKey, orderId, Number(amount));
        console.log("[TOSS] paymentSuccess 호출 완료");
        setIsLoading(false);
      } catch (error) {
        console.error("결제 처리 중 오류 발생:", error);
        const err = error as any;
        if (err?.response?.data) {
          console.error("백엔드 응답:", err.response.data);
        }
        setError("결제 처리 중 오류가 발생했습니다.");
        setIsLoading(false);
      }
    };

    processPayment();
  }, [searchParams]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">결제를 처리하고 있습니다...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-2xl mb-4">결제 실패</div>
          <p className="text-gray-600 mb-6">{error}</p>
          <Link
            href="/cart"
            className="inline-block bg-orange-500 text-white px-6 py-2 rounded hover:bg-orange-600"
          >
            장바구니로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-center">
        <div className="text-2xl font-bold text-orange-500 mb-4">
          결제가 완료되었습니다!
        </div>
        <p className="text-gray-600 mb-6">
          주문하신 상품은 빠르게 배송해드리겠습니다.
        </p>
        <div className="space-x-4">
          <Link
            href="/orders"
            className="inline-block bg-orange-500 text-white px-6 py-2 rounded hover:bg-orange-600"
          >
            주문내역 보기
          </Link>
          <Link
            href="/"
            className="inline-block bg-gray-200 text-gray-700 px-6 py-2 rounded hover:bg-gray-300"
          >
            쇼핑 계속하기
          </Link>
        </div>
      </div>
    </div>
  );
}
