package com.bubble.giju.domain.address.service.impl;

import com.bubble.giju.domain.address.dto.AddressDto;
import com.bubble.giju.domain.address.entity.Address;
import com.bubble.giju.domain.address.repository.AddressRepository;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.domain.user.repository.UserRepository;
import com.bubble.giju.global.config.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {
    @InjectMocks
    private AddressServiceImpl addressService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    private String testUserId;
    private UUID testUUID;
    private AddressDto.Request request;
    private User testUser;
    private Long testAddressId;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        testUUID = UUID.fromString(testUserId);
        testUser = User.builder()
                .userId(testUUID)
                .loginId("user")
                .build();

        request = new AddressDto.Request(
                "수령인", "01012345678", "집", true,
                12345, "서울특별시 강남구", "아파트", "101동 1001호"
        );

        testAddressId = 1L;
        testAddress = Address.builder()
                .id(testAddressId)
                .recipientName("수령인")
                .user(testUser)
                .defaultAddress(true)
                .build();
    }

    @Test
    @DisplayName("주소 저장 - 성공 - 기본배송지 없을 때")
    void createAddress() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));
        given(addressRepository.findByUser_UserIdAndDefaultAddressTrue(testUUID))
                .willReturn(Optional.empty());

        // when
        AddressDto.Response response = addressService.createAddress(testUserId, request);

        // then
        assertNotNull(response);
        verify(addressRepository).findByUser_UserIdAndDefaultAddressTrue(testUUID);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("주소 저장 - 성공 - 기본배송지 비활성화 호출 확인")
    void createAddress_기본배송지_비활성화() {
        // given
        Address oldDefault = mock(Address.class);
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));
        given(addressRepository.findByUser_UserIdAndDefaultAddressTrue(testUUID))
                .willReturn(Optional.of(oldDefault));

        // when
        addressService.createAddress(testUserId, request);

        // then
        verify(oldDefault).updateDefaultAddressToFalse();
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("주소 저장 - 성공 - 기본배송지 false")
    void createAddress_기본배송지_false() {
        // given
        AddressDto.Request falseRequest = new AddressDto.Request(
                "수령인", "01012345678", "집", false,
                12345, "서울특별시 강남구", "아파트", "101동 1001호"
        );

        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));

        // when
        AddressDto.Response response = addressService.createAddress(testUserId, falseRequest);

        // then
        assertNotNull(response);
        verify(addressRepository, never()).findByUser_UserIdAndDefaultAddressTrue(testUUID);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("주소 저장 - 실패 - 유저 없음")
    void createAddress_유저없음() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.empty());

        // when & then
        CustomException customException = assertThrows(CustomException.class, () ->
                addressService.createAddress(testUserId, request));

        // then
        verify(userRepository).findById(eq(testUUID));
        assertNotNull(customException);
        assertEquals("사용자가 권한이없음", customException.getMessage());
    }

    @Test
    @DisplayName("주소 불러오기 - 성공")
    void getAddress_success() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));
        given(addressRepository.findByUser_UserId(testUUID)).willReturn(List.of(testAddress));

        // when
        List<AddressDto.Response> addressList = addressService.getAddress(testUserId);

        // then
        assertNotNull(addressList);
        verify(addressRepository).findByUser_UserId(testUUID);
        assertEquals(1, addressList.size());
        assertEquals("수령인", addressList.getFirst().getRecipientName());
    }

    @Test
    @DisplayName("주소 불러오기 - 실패")
    void getAddress_fail() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.empty());

        // when
        CustomException customException = assertThrows(CustomException.class, () ->
                addressService.getAddress(testUserId));

        // then
        assertNotNull(customException);
        verify(userRepository).findById(eq(testUUID));
    }

    @Test
    @DisplayName("주소 업데이트 - 성공 - 기본배송지 false")
    void updateAddress_success_기본배송지_false() {
        // given
        Address false_Address = Address.builder()
                .id(testAddressId)
                .user(testUser)
                .defaultAddress(false)
                .build();

        AddressDto.Request updateRequest = new AddressDto.Request(
                "수정된 수령인", "01011112222", "회사", false,
                99999, "서울시 중구", "타워", "3층"
        );

        given(addressRepository.findByIdAndUser_UserId(testAddressId, testUUID))
                .willReturn(Optional.of(false_Address));

        // when
        AddressDto.Response response = addressService.updateAddress(testUserId, testAddressId, updateRequest);

        // then
        assertNotNull(response);
        verify(addressRepository, never()).findByUser_UserIdAndDefaultAddressTrue(testUUID);
        verify(addressRepository).findByIdAndUser_UserId(testAddressId, testUUID);
    }

    @Test
    @DisplayName("주소 업데이트 - 성공 - 기본배송지 true")
    void updateAddress_success_기본배송지_true() {
        // given
        AddressDto.Request updateRequest = new AddressDto.Request(
                "수정된 수령인", "01011112222", "회사", true,
                99999, "서울시 중구", "타워", "3층"
        );

        // 기존 기본배송지를 false로 바꾸는 address
        Address previousDefaultAddress = Address.builder()
                .id(99L)
                .user(testUser)
                .defaultAddress(true)
                .build();


        given(addressRepository.findByIdAndUser_UserId(testAddressId, testUUID))
                .willReturn(Optional.of(testAddress));
        given(addressRepository.findByUser_UserIdAndDefaultAddressTrue(testUUID))
                .willReturn(Optional.of(previousDefaultAddress));

        // when
        AddressDto.Response response = addressService.updateAddress(testUserId, testAddressId, updateRequest);

        // then
        assertNotNull(response);
        assertEquals("수정된 수령인", response.getRecipientName());
        assertTrue(response.isDefaultAddress());
        assertFalse(previousDefaultAddress.isDefaultAddress());
        verify(addressRepository).findByUser_UserIdAndDefaultAddressTrue(testUUID);
        verify(addressRepository).findByIdAndUser_UserId(testAddressId, testUUID);
    }

    @Test
    @DisplayName("주소 업데이트 - 실패 - 주소 아이디 없음")
    void updateAddress_fail_주소없음() {
        // given
        given(addressRepository.findByIdAndUser_UserId(testAddressId, testUUID))
                .willReturn(Optional.empty());

        // when & then
        CustomException customException = assertThrows(CustomException.class, () ->
                addressService.updateAddress(testUserId, testAddressId, request));

        assertNotNull(customException);
        assertEquals("사용자가 권한이없음", customException.getMessage());
    }

    @Test
    @DisplayName("주소 업데이트 - 실패 - 기본배송지_해제불가")
    void updateAddress_fail_기본배송지_해제불가() {
        // given
        AddressDto.Request updateRequest = new AddressDto.Request(
                "수정된 수령인", "01011112222", "회사", false,
                99999, "서울시 중구", "타워", "3층"
        );

        given(addressRepository.findByIdAndUser_UserId(testAddressId, testUUID))
                .willReturn(Optional.of(testAddress));

        // when & then
        CustomException customException = assertThrows(CustomException.class, () ->
                addressService.updateAddress(testUserId, testAddressId, updateRequest));

        assertNotNull(customException);
        assertEquals("기본 배송지는 해제할 수 없습니다.", customException.getMessage());
    }

    @Test
    @DisplayName("주소 삭제 - 성공")
    void deleteAddress_success() {
        // gievn
        Address falseAddress = Address.builder()
                .id(testAddressId)
                .defaultAddress(false)
                .build();
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));
        given(addressRepository.findByIdAndUser_UserId(testAddressId, testUUID))
                .willReturn(Optional.of(falseAddress));

        // when
        Long returnAddressId = addressService.deleteAddress(testUserId, testAddressId);

        // then
        assertNotNull(returnAddressId);
        assertEquals(testAddressId, returnAddressId);
        verify(addressRepository).findByIdAndUser_UserId(testAddressId, testUUID);
    }

    @Test
    @DisplayName("주소 삭제 - 실패 - 유저 없음")
    void deleteAddress_fail_유저없음() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.empty());

        // when & then
        CustomException customException = assertThrows(CustomException.class, () ->
                addressService.deleteAddress(testUserId, testAddressId));

        assertNotNull(customException);
        assertEquals("사용자가 권한이없음", customException.getMessage());
    }

    @Test
    @DisplayName("주소 삭제 - 실패 - 주소없음")
    void deleteAddress_fail_주소없음() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));
        given(addressRepository.findByIdAndUser_UserId(testAddressId, testUUID))
                .willReturn(Optional.empty());

        // when & then
        CustomException customException = assertThrows(CustomException.class, () ->
                addressService.deleteAddress(testUserId, testAddressId));

        assertNotNull(customException);
        assertEquals("사용자가 권한이없음", customException.getMessage());
    }

    @Test
    @DisplayName("주소 삭제 - 실패 - 기본배송지")
    void deleteAddress_fail_기본배송지() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));
        given(addressRepository.findByIdAndUser_UserId(testAddressId, testUUID))
                .willReturn(Optional.of(testAddress));

        // when & then
        CustomException customException = assertThrows(CustomException.class, () ->
                addressService.deleteAddress(testUserId, testAddressId));

        assertNotNull(customException);
        assertEquals("기본 배송지는 삭제할 수 없습니다. 다른 배송지를 기본으로 설정한 후 삭제해주세요.", customException.getMessage());
    }

    @Test
    @DisplayName("기본 배송지 불러오기 - 성공 - 존재")
    void getDefaultAddress_success_존재() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));
        given(addressRepository.findByUser_UserIdAndDefaultAddressTrue(testUUID))
                .willReturn(Optional.of(testAddress));

        // when
        AddressDto.Response defaultAddress = addressService.getDefaultAddress(testUserId);

        // then
        assertNotNull(defaultAddress);
        assertEquals(testAddressId, defaultAddress.getAddressId());
    }

    @Test
    @DisplayName("기본 배송지 불러오기 - 성공 - 없음")
    void getDefaultAddress_success_없음() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.of(testUser));
        given(addressRepository.findByUser_UserIdAndDefaultAddressTrue(testUUID))
                .willReturn(Optional.empty());

        // when
        AddressDto.Response defaultAddress = addressService.getDefaultAddress(testUserId);

        // then
        assertNull(defaultAddress);
    }

    @Test
    @DisplayName("기본 배송지 불러오기 - 실패 - 유저 없음")
    void getDefaultAddress_fail_유저없음() {
        // given
        given(userRepository.findById(eq(testUUID))).willReturn(Optional.empty());

        // when & then
        CustomException customException = assertThrows(CustomException.class, () ->
                addressService.getDefaultAddress(testUserId));

        assertNotNull(customException);
        assertEquals("사용자가 권한이없음", customException.getMessage());
    }

}