"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { paymentFail } from "@/lib/payment";
import Link from "next/link";

export default function PaymentFailPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const processPaymentFail = async () => {
      try {
        const code = searchParams.get("code");
        const message = searchParams.get("message");
        const orderId = searchParams.get("orderId");

        if (!code || !message || !orderId) {
          throw new Error("필수 결제 정보가 누락되었습니다.");
        }

        await paymentFail(code, message, orderId);
        setError(message);
        setIsLoading(false);
      } catch (error) {
        console.error("결제 실패 처리 중 오류 발생:", error);
        setError("결제 실패 처리 중 오류가 발생했습니다.");
        setIsLoading(false);
      }
    };

    processPaymentFail();
  }, [searchParams]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">결제 실패를 처리하고 있습니다...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-center">
        <div className="text-red-500 text-2xl mb-4">결제 실패</div>
        <p className="text-gray-600 mb-6">{error}</p>
        <div className="space-x-4">
          <Link
            href="/cart"
            className="inline-block bg-orange-500 text-white px-6 py-2 rounded hover:bg-orange-600"
          >
            장바구니로 돌아가기
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
