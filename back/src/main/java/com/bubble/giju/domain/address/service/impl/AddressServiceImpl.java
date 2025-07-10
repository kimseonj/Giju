package com.bubble.giju.domain.address.service.impl;

import com.bubble.giju.domain.address.dto.AddressDto;
import com.bubble.giju.domain.address.entity.Address;
import com.bubble.giju.domain.address.repository.AddressRepository;
import com.bubble.giju.domain.address.service.AddressService;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AddressServiceImpl implements AddressService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public AddressDto.Response createAddress(String userId, AddressDto.Request request) {
        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

        boolean isDefaultAddress = false;
        // 기본배송지로 설정
        if (request.isDefaultAddress()) {
            isDefaultAddress = true;

            // 기존 기본배송지를 false로 변경
            addressRepository.findByUser_UserIdAndDefaultAddressTrue(UUID.fromString(userId))
                    .ifPresent(Address::updateDefaultAddressToFalse);
        }

        Address address = Address.builder()
                .user(user)
                .recipientName(request.getRecipientName())
                .phoneNumber(request.getPhoneNumber())
                .alias(request.getAlias())
                .defaultAddress(isDefaultAddress)
                .postcode(request.getPostcode())
                .roadAddress(request.getRoadAddress())
                .buildingName(request.getBuildingName())
                .detailAddress(request.getDetailAddress())
                .build();

        addressRepository.save(address);

        return AddressDto.Response.fromEntity(address);
    }

    public List<AddressDto.Response> getAddress(String userId) {
        userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

         return addressRepository.findByUser_UserId(UUID.fromString(userId))
                 .stream().map(AddressDto.Response::fromEntity).toList();
    }

    @Transactional
    @Override
    public AddressDto.Response updateAddress(String userId, Long addressId, AddressDto.Request request) {
        Address address = addressRepository.findByIdAndUser_UserId(addressId, UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

        // 기본 배송지 해제 불가
        if (address.isDefaultAddress() && !request.isDefaultAddress()) {
            throw new CustomException(ErrorCode.INVALID_DEFAULT_ADDRESS);
        }

        // 기본배송지로 설정
        if (request.isDefaultAddress()) {
            // 기존 기본배송지를 false로 변경
            addressRepository.findByUser_UserIdAndDefaultAddressTrue(UUID.fromString(userId))
                    .ifPresent(Address::updateDefaultAddressToFalse);
        }

        // 주소 수정
        address.update(request);

        return AddressDto.Response.fromEntity(address);
    }

    @Transactional
    @Override
    public Long deleteAddress(String userId, Long addressId) {
        userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

        Address address = addressRepository.findByIdAndUser_UserId(addressId, UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

        if (address.isDefaultAddress()) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS);
        }

        addressRepository.delete(address);
        return addressId;
    }

    @Override
    public AddressDto.Response getDefaultAddress(String userId) {
        userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new CustomException(ErrorCode.USER_UNAUTHORIZED)
        );

        return addressRepository.findByUser_UserIdAndDefaultAddressTrue(UUID.fromString(userId))
                .map(AddressDto.Response::fromEntity)
                .orElse(null);
    }
}
