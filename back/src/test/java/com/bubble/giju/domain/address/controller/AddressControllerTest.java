package com.bubble.giju.domain.address.controller;

import com.bubble.giju.domain.address.dto.AddressDto;
import com.bubble.giju.domain.address.entity.Address;
import com.bubble.giju.domain.address.service.AddressService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.domain.user.entity.User;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import com.bubble.giju.global.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AddressController.class,
        excludeAutoConfiguration = {SecurityConfig.class})
class AddressControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    private String testUserId;
    private UUID testUUID;
    private User testUser;
    private Long testAddressId;
    private CustomPrincipal customPrincipal;
    private AddressDto.Request request;
    private AddressDto.Response response;

    @BeforeEach
    void setUp() {
        testUUID = UUID.randomUUID();
        testUserId = testUUID.toString();
        testAddressId = 1L;

        testUser = User.builder()
                .userId(testUUID)
                .loginId("loginId")
                .build();

        customPrincipal = new CustomPrincipal(testUser);

        request = new AddressDto.Request(
                "recipientName", "phoneNumber", "alias", true,
                1111, "roadAddress", "buildingName", "detailAddress"
        );

        response = new AddressDto.Response(
                testAddressId, testUserId, "recipientName", "phoneNumber",
                "alias", true, 1111, "roadAddress", "buildingName", "detailAddress"
        );
    }

    @Test
    @DisplayName("주소 저장 - 성공")
    void createAddress() throws Exception {
        // given
        given(addressService.createAddress(eq(testUserId), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/address")
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))) // JSON body
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("주소 저장 완료"))
                .andExpect(jsonPath("$.data.userId").value(testUserId));
    }

    @Test
    @DisplayName("주소 저장 - 실패 - 유저 없음")
    void createAddress_fail() throws Exception {
        // given
        given(addressService.createAddress(eq(testUserId), any()))
                .willThrow(new CustomException(ErrorCode.USER_UNAUTHORIZED));

        // when & then
        mockMvc.perform(post("/api/address")
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))) // JSON body
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("사용자가 권한이없음"));
    }

    @Test
    @DisplayName("주소 불러오기 - 성공")
    void getAddress() throws Exception {
        // given
        given(addressService.getAddress(eq(testUserId)))
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/address")
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(testUserId));
    }

    @Test
    @DisplayName("주소 불러오기 - 실패")
    void getAddress_fail() throws Exception {
        // given
        given(addressService.getAddress(eq(testUserId)))
                .willThrow(new CustomException(ErrorCode.USER_UNAUTHORIZED));

        // when & then
        mockMvc.perform(get("/api/address")
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("사용자가 권한이없음"));
    }

    @Test
    @DisplayName("주소 수정하기 - 성공")
    void updateAddress() throws Exception {
        // given
        // 수정용 Request
        AddressDto.Request updateRequest = new AddressDto.Request(
                "updatedRecipient", "updatedPhone", "updatedAlias", false,
                2222, "updatedRoadAddress", "updatedBuilding", "updatedDetail"
        );

        // 수정 후 예상 Response
        AddressDto.Response updateResponse = new AddressDto.Response(
                1L, testUserId, "updatedRecipient", "updatedPhone",
                "updatedAlias", false, 2222, "updatedRoadAddress", "updatedBuilding", "updatedDetail"
        );

        given(addressService.updateAddress(eq(testUserId), eq(testAddressId), any()))
                .willReturn(updateResponse);

        // when & then
        mockMvc.perform(patch("/api/address/{addressId}", testAddressId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("주소 수정 완료"))
                .andExpect(jsonPath("$.data.userId").value(testUserId))
                .andExpect(jsonPath("$.data.recipientName").value("updatedRecipient"));
    }

    @Test
    @DisplayName("주소 수정하기 - 실패 - 주소 없음")
    void updateAddress_fail() throws Exception {
        // given
        // 수정용 Request
        AddressDto.Request updateRequest = new AddressDto.Request(
                "updatedRecipient", "updatedPhone", "updatedAlias", false,
                2222, "updatedRoadAddress", "updatedBuilding", "updatedDetail"
        );

        given(addressService.updateAddress(eq(testUserId), eq(testAddressId), any()))
                .willThrow(new CustomException(ErrorCode.USER_UNAUTHORIZED));

        // when & then
        mockMvc.perform(patch("/api/address/{addressId}", testAddressId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("사용자가 권한이없음"));
    }

    @Test
    @DisplayName("주소 수정하기 - 실패 - 기본 주소 해제 불가")
    void updateAddress_fail2() throws Exception {
        // given
        // 수정용 Request
        AddressDto.Request updateRequest = new AddressDto.Request(
                "updatedRecipient", "updatedPhone", "updatedAlias", false,
                2222, "updatedRoadAddress", "updatedBuilding", "updatedDetail"
        );

        given(addressService.updateAddress(eq(testUserId), eq(testAddressId), any()))
                .willThrow(new CustomException(ErrorCode.INVALID_DEFAULT_ADDRESS));

        // when & then
        mockMvc.perform(patch("/api/address/{addressId}", testAddressId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("기본 배송지는 해제할 수 없습니다."));
    }

    @Test
    @DisplayName("주소 삭제하기 - 성공")
    void deleteAddress() throws Exception {
        // given
        given(addressService.deleteAddress(eq(testUserId), eq(testAddressId)))
                .willReturn(testAddressId);

        // when & then
        mockMvc.perform(delete("/api/address/{addressId}", testAddressId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("주소 삭제하기 - 실패 - 주소 없음")
    void deleteAddress_fail12() throws Exception {
        // given
        given(addressService.deleteAddress(eq(testUserId), eq(testAddressId)))
                .willThrow(new CustomException(ErrorCode.USER_UNAUTHORIZED));

        // when & then
        mockMvc.perform(delete("/api/address/{addressId}", testAddressId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("사용자가 권한이없음"));
    }

    @Test
    @DisplayName("주소 삭제하기 - 실패 - 기본배송지 삭제")
    void deleteAddress_fail3() throws Exception {
        // given
        given(addressService.deleteAddress(eq(testUserId), eq(testAddressId)))
                .willThrow(new CustomException(ErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS));

        // when & then
        mockMvc.perform(delete("/api/address/{addressId}", testAddressId)
                        .with(user(customPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("기본 배송지는 삭제할 수 없습니다. 다른 배송지를 기본으로 설정한 후 삭제해주세요."));
    }
}