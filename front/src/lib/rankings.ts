import apiClient from "./api-client";

/**
 * 지역별 랭킹 조회 (상위 10명)
 * @param regionCode - 지역 코드 (필수)
 * @returns {Promise<{ region: string, ranking: Array<{ name: string, totalQuantity: number }> }>} 지역명과 상위 10명의 랭킹 정보
 * @example
 * const data = await getRankings(1);
 * console.log(data.region); // 지역명
 * console.log(data.ranking); // [{ name, totalQuantity }, ...]
 */
export async function getRankings(regionCode: number) {
  const response = await apiClient.get("/rankings", {
    params: { regionCode },
  });
  return response.data;
}
