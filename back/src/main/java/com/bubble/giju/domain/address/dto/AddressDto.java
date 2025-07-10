package com.bubble.giju.domain.address.dto;

import com.bubble.giju.domain.address.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class AddressDto {

    @AllArgsConstructor
    @Getter
    public static class Request {
        private String recipientName;
        private String phoneNumber;

        private String alias;
        private boolean defaultAddress;

        private int postcode;
        private String roadAddress;
        private String buildingName;

        private String detailAddress;
    }


    @AllArgsConstructor
    @Getter
    public static class Response {
        private Long addressId;
        private String userId;

        private String recipientName;
        private String phoneNumber;

        private String alias;
        private boolean defaultAddress;

        private int postcode;
        private String roadAddress;
        private String buildingName;

        private String detailAddress;

        public static Response fromEntity(Address address) {
            return new Response(
                    address.getId(),
                    address.getUser().getUserId().toString(),
                    address.getRecipientName(),
                    address.getPhoneNumber(),
                    address.getAlias(),
                    address.isDefaultAddress(),
                    address.getPostcode(),
                    address.getRoadAddress(),
                    address.getBuildingName(),
                    address.getDetailAddress()
            );
        }

    }
}
