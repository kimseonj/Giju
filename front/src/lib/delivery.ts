import apiClient from "./api-client";

/**
 * 택배회사 데이터 수정
 * @param deliveryCompanyId 수정할 택배회사 ID
 * @param name 새로운 택배회사 이름
 * @returns {Promise<any>} 수정 결과
 * @example
 * await updateDeliveryCompany(1, "CJ대한통운");
 */
export async function updateDeliveryCompany(
  deliveryCompanyId: number,
  name: string
) {
  const response = await apiClient.put(
    `/admin/delivery-company/${deliveryCompanyId}`,
    { name }
  );
  return response.data;
}

/**
 * 택배회사 데이터 삭제
 * @param deliveryCompanyId 삭제할 택배회사 ID
 * @returns {Promise<any>} 삭제 결과
 * @example
 * await deleteDeliveryCompany(1);
 */
export async function deleteDeliveryCompany(deliveryCompanyId: number) {
  const response = await apiClient.delete(
    `/admin/delivery-company/${deliveryCompanyId}`
  );
  return response.data;
}

/**
 * 택배회사 리스트 조회
 * @returns {Promise<any[]>} 택배회사 정보 배열
 * @example
 * const companies = await getDeliveryCompanies();
 */
export async function getDeliveryCompanies() {
  const response = await apiClient.get("/delivery-companies");
  return response.data;
}
