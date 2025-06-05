import apiClient from "./api-client";

/**
 * 술(상품) 등록
 * @param drink 상품 정보 객체 (name, price, stock, alcoholContent, volume, region, categoryId)
 * @param thumbnail 썸네일 이미지 파일 (File)
 * @param files 추가 이미지 파일 배열 (File[])
 * @returns {Promise<any>} 등록 결과
 */
export async function createDrink(drink: any, thumbnail: File, files: File[]) {
  const formData = new FormData();
  formData.append("thumbnail", thumbnail);
  files.forEach((file) => formData.append("files", file));
  formData.append(
    "drink",
    new Blob([JSON.stringify(drink)], { type: "application/json" })
  );

  const response = await apiClient.post("/admin/drink", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return response.data;
}

/**
 * 술(상품) 삭제
 * @param drinkId 삭제할 상품 ID
 * @returns {Promise<any>} 삭제 결과
 */
export async function deleteDrink(drinkId: number) {
  const response = await apiClient.delete(`/admin/drink/${drinkId}`);
  return response.data;
}

/**
 * 술(상품) 업데이트
 * @param drinkId 수정할 상품 ID
 * @param data { price, stock, region, categoryId }
 * @returns {Promise<any>} 수정 결과
 */
export async function updateDrink(drinkId: number, data: any) {
  const response = await apiClient.patch(`/admin/drink/${drinkId}`, data);
  return response.data;
}

/**
 * 삭제된 술(상품) 재판매
 * @param drinkId 재판매할 상품 ID
 * @returns {Promise<any>} 재판매 결과
 */
export async function restoreDrink(drinkId: number) {
  const response = await apiClient.patch(`/admin/drink/${drinkId}/restore`);
  return response.data;
}

/**
 * 술(상품) 단일조회
 * @param drinkId 조회할 상품 ID
 * @returns {Promise<any>} 상품 상세 정보
 */
export async function getDrink(drinkId: number) {
  const response = await apiClient.get(`/drink/${drinkId}`);
  return response.data;
}

/**
 * 술(상품) 검색
 * @param params 검색 파라미터
 * @param params.type 검색 타입 (category: 카테고리 ID, region: 지역명, name: 술 이름)
 * @param params.keyword 검색 키워드 (category: 카테고리 ID 값, region: 지역명, name: 술 이름)
 * @param params.pageNum 페이지 번호 (기본값: 1)
 * @returns 검색 결과
 *
 * @example
 * // 카테고리로 검색
 * searchDrinks({ type: 'category', keyword: '1' })
 *
 * // 지역으로 검색
 * searchDrinks({ type: 'region', keyword: '서울' })
 *
 * // 이름으로 검색 (부분 일치)
 * searchDrinks({ type: 'name', keyword: '막걸리' })
 */
export async function searchDrinks(params: {
  type: "category" | "region" | "name";
  keyword: string;
  pageNum?: number;
}) {
  const requestParams = {
    type: params.type,
    keyword: params.keyword,
    ...(params.pageNum && { pageNum: params.pageNum }),
  };

  const response = await apiClient.get("/drinks", {
    params: requestParams,
  });

  return response.data;
}
