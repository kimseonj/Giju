import apiClient from "./api-client";

/**
 * 장바구니 아이템 타입
 */
export interface CartItem {
  cartId: number;
  drinkId: number;
  drinkName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  imageUrl?: string;
}

/**
 * 장바구니 전체 조회 응답 타입
 */
export interface CartResponse {
  items: CartItem[];
  totalPrice: number;
  deliveryCharge: number;
  totalPriceWithDelivery: number;
}

/**
 * 장바구니 전체 조회
 * @returns {Promise<CartResponse>} 장바구니 전체 정보
 * @example
 * const cart = await getCart();
 * console.log(cart.items);
 */
export async function getCart(): Promise<CartResponse> {
  const response = await apiClient.get<CartResponse>("/cart");
  return response.data;
}

/**
 * 장바구니 상품 수량 변경
 * @param cartId 장바구니 아이템 ID
 * @param quantity 변경할 수량
 * @returns {Promise<any>} 변경 결과
 */
export async function updateCartItemQuantity(cartId: number, quantity: number) {
  const response = await apiClient.patch(`/cart/${cartId}`, { quantity });
  return response.data;
}

/**
 * 장바구니 구매 정보 조회
 * @param cartItemIds 선택된 cartId 배열
 * @returns {Promise<any>} 구매 정보 (총 금액, 배송비 등)
 * @example
 * const info = await getCartBuyInfo([1, 2, 3]);
 */
export async function getCartBuyInfo(cartItemIds: number[]) {
  try {
    console.log("[Cart] 구매 정보 조회 시작", { cartItemIds });
    const response = await apiClient.post(`/cart/buy`, {
      cartIds: cartItemIds,
    });
    console.log("[Cart] 구매 정보 조회 완료", response.data);
    return response.data;
  } catch (error: any) {
    console.error("[Cart] 구매 정보 조회 실패:", {
      error,
      response: error.response?.data,
      status: error.response?.status,
      headers: error.response?.headers,
    });
    throw error;
  }
}

/**
 * 선택된 장바구니 아이템 삭제
 * @param cartIds 삭제할 cartId 배열
 * @returns {Promise<any>} 삭제 결과
 */
export async function deleteCartItems(cartIds: number[]) {
  const response = await apiClient.delete(`/cart/delete`, {
    data: { cartIds },
  } as any);
  return response.data;
}

/**
 * 장바구니 상품 추가
 * @param drinkId 추가할 drinkId 배열
 * @param quantity 추가할 quantity 배열
 * @returns {Promise<any>} 상품 추가 정보
 */
export async function addCartItems(drinkId: number, quantity: number) {
  const response = await apiClient.post(`/cart/add`, { drinkId, quantity });
  return response.data;
}
