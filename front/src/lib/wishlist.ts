import apiClient from "./api-client";

/**
 * 상품 찜하기(위시리스트 추가)
 * @param drinkId 찜할 상품의 ID
 * @returns {Promise<any>} API 응답 데이터
 * @example
 * await addWishList(101);
 */
export async function addWishList(drinkId: number) {
  const response = await apiClient.post(`/drinks/${drinkId}/wishlist`);
  return response.data;
}

/**
 * 삼품 찜하기 제거(위시리스트 제거)
 * @param drinkId 삭제할 상품의 ID
 * @returns {Promise<any>} API 응답 데이터
 * @example
 * await deleteWishList(101);
 */
export async function deleteWishList(drinkId: number) {
  const response = await apiClient.delete(`/drinks/${drinkId}/wishlist`);
  return response.data;
}

/**
 * 찜하기 목록 조회(위시리스트 조회)
 * @returns {Promise<any>} API 응답 데이터
 * @example
 * await getWishList();
 */
export async function getWishList() {
  const response = await apiClient.get(`/users/me/wishlist`);
  return response.data;
}
