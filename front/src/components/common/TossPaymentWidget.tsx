import { useEffect, useRef, useState } from "react";
import { loadPaymentWidget } from "@tosspayments/payment-widget-sdk";

interface TossPaymentWidgetProps {
  amount: number;
  orderInfo: {
    orderId: string;
    orderName: string;
    customerName: string;
    customerEmail: string;
  };
}

const clientKey = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY!;
const customerKey =
  typeof window !== "undefined"
    ? localStorage.getItem("userId") || "guest"
    : "guest";

export default function TossPaymentWidget({
  amount,
  orderInfo,
}: TossPaymentWidgetProps) {
  const paymentWidgetRef = useRef<any>(null);
  const [widgetLoaded, setWidgetLoaded] = useState(false);

  useEffect(() => {
    (async () => {
      if (!clientKey || !customerKey) return;
      const paymentWidget = await loadPaymentWidget(clientKey, customerKey);
      paymentWidgetRef.current = paymentWidget;
      await paymentWidget.renderPaymentMethods("#payment-method", amount);
      await paymentWidget.renderAgreement("#agreement");
      setWidgetLoaded(true);
    })();
  }, [amount]);

  const handlePayment = async () => {
    if (!paymentWidgetRef.current) return;
    try {
      await paymentWidgetRef.current.requestPayment({
        orderId: orderInfo.orderId,
        orderName: orderInfo.orderName,
        customerName: orderInfo.customerName,
        customerEmail: orderInfo.customerEmail,
        successUrl: "https://giju.vercel.app/payment/success",
        failUrl: "https://giju.vercel.app/payment/fail",
      });
    } catch (e) {
      alert("결제 요청 중 오류가 발생했습니다.");
      console.error(e);
    }
  };

  return (
    <div>
      <div id="payment-method" />
      <div id="agreement" />
      <button
        onClick={handlePayment}
        disabled={!widgetLoaded}
        className="w-[140px] h-[68px] mt-6 text-white rounded flex items-center justify-center hover:opacity-90 transition"
        style={{
          backgroundImage: "url('/cart-button.svg')",
          backgroundSize: "100% 100%",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          border: "none",
        }}
      >
        결제하기
      </button>
    </div>
  );
}
