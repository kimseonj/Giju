import apiClient from "./api-client";

/**
 * 결제 성공(토스 콜백) 처리
 * @param paymentKey 결제 키 (Toss에서 전달)
 * @param orderId 주문 ID (Toss에서 전달)
 * @param amount 결제 금액 (Toss에서 전달)
 * @returns {Promise<string>} 결제 승인 결과
 * @example
 * await paymentSuccess("pay_abc", "order_123", 50000);
 */
export async function paymentSuccess(
  paymentKey: string,
  orderId: string,
  amount: number
) {
  const response = await apiClient.get("/payment/success", {
    params: { paymentKey, orderId, amount },
  });
  return response.data;
}

/**
 * 결제 실패(토스 콜백) 처리
 * @param code 실패 코드 (Toss에서 전달)
 * @param message 실패 메시지 (Toss에서 전달)
 * @param orderId 주문 ID (Toss에서 전달)
 * @returns {Promise<string>} 결제 실패 메시지
 * @example
 * await paymentFail("ERROR_CODE", "결제 실패 메시지", "order_123");
 */
export async function paymentFail(
  code: string,
  message: string,
  orderId: string
) {
  const response = await apiClient.get("/payment/fail", {
    params: { code, message, orderId },
  });
  return response.data;
}

/**
 * 결제 취소 API 호출
 * @param orderId - 취소할 주문 ID
 * @param canceledItems - 취소할 주문 상세 항목 배열 (orderDetailId, cancelAmount)
 * @param cancelReason - 취소 사유
 * @returns {Promise<any>} 취소 결과 (success, message, data 등)
 * @example
 * const result = await cancelPayment(1, [{ orderDetailId: 1, cancelAmount: 1000 }], "고객 요청");
 */
export async function cancelPayment(
  orderId: number,
  canceledItems: Array<{ orderDetailId: number; cancelAmount: number }>,
  cancelReason: string
) {
  const response = await apiClient.post(`/payment/${orderId}/cancel`, {
    orderId,
    canceledItems,
    cancelReason,
  });
  return response.data;
}
