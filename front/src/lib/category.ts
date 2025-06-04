import apiClient from "./api-client";

/**
 * 카테고리 전체 조회
 * @returns {Promise<{id: number, name: string}[]>}
 */
export async function getCategories() {
  const response = await apiClient.get("/categories");
  return response.data;
}
