export interface Address {
  addressId: number;
  recipientName: string;
  phoneNumber: string;
  roadAddress: string;
  detailAddress: string;
  alias: string;
  defaultAddress: boolean;
  postcode: number;
  buildingName?: string;
  extraAddress?: string;
  request?: string;
}
