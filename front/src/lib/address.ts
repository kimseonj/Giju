import apiClient from "./api-client";
import { Address } from "@/types/address";

/**
 * 유저의 모든 주소 리스트 조회
 * @returns {Promise<any[]>} 주소 정보 배열
 * @example
 * const addresses = await getAddressList();
 */
export async function getAddressList() {
  const response = await apiClient.get("/address");
  return response.data;
}

// API 명세에 맞는 주소 저장/수정용 타입
export type AddressPayload = {
  recipientName: string;
  phoneNumber: string;
  alias: string;
  defaultAddress: boolean;
  postcode: number;
  roadAddress: string;
  buildingName: string;
  detailAddress: string;
};

/**
 * 회원 주소 저장 및 등록
 * @param address 저장할 주소 정보
 * @returns {Promise<any>} 저장 결과
 * @example
 * await saveAddress({
 *   recipientName: "홍길동",
 *   phoneNumber: "010-1234-5678",
 *   alias: "집",
 *   defaultAddress: true,
 *   postcode: 12345,
 *   roadAddress: "서울시 강남구 테헤란로 1",
 *   buildingName: "삼성빌딩",
 *   detailAddress: "101호"
 * });
 */
export async function saveAddress(address: AddressPayload) {
  const response = await apiClient.post("/address", address);
  return response.data;
}

/**
 * 주소 삭제
 * @param addressId 삭제할 주소의 ID
 * @returns {Promise<any>} 삭제 결과
 * @example
 * await deleteAddress(123);
 */
export async function deleteAddress(addressId: number) {
  const response = await apiClient.delete(`/address/${addressId}`);
  return response.data;
}

/**
 * 주소 수정
 * @param addressId 수정할 주소의 ID
 * @param address 수정할 주소 정보
 * @returns {Promise<any>} 수정 결과
 * @example
 * await updateAddress(123, {
 *   recipientName: "홍길동",
 *   phoneNumber: "010-1234-5678",
 *   alias: "회사",
 *   defaultAddress: false,
 *   postcode: 54321,
 *   roadAddress: "서울시 강남구 역삼로 2",
 *   buildingName: "역삼빌딩",
 *   detailAddress: "202호"
 * });
 */
export async function updateAddress(
  addressId: number,
  address: AddressPayload
) {
  const response = await apiClient.patch(`/address/${addressId}`, address);
  return response.data;
}
