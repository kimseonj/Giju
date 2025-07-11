package com.bubble.giju.domain.address.controller;

import com.bubble.giju.domain.address.dto.AddressDto;
import com.bubble.giju.domain.address.service.AddressService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.global.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "주소 API", description = "주소 관련 API 입니다.")
@RequiredArgsConstructor
@RequestMapping("/api/address")
@RestController
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "주소 저장", description = "회원의 주소를 저장합니다.")
    @PostMapping("")
    public ResponseEntity<ApiResponse<?>> createAddress(@AuthenticationPrincipal CustomPrincipal customPrincipal, @RequestBody AddressDto.Request request) {
        AddressDto.Response address = addressService.createAddress(customPrincipal.getUserId(), request);

        ApiResponse<AddressDto.Response> apiResponse = ApiResponse.success("주소 저장 완료", address);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Operation(summary = "주소 리스트 불러오기", description = "UerID를 이용한 유저의 모든 주소 불러오기")
    @GetMapping("")
    public ResponseEntity<List<AddressDto.Response>> getAddress(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return new ResponseEntity<>(addressService.getAddress(customPrincipal.getUserId()), HttpStatus.OK);
    }

    @Operation(summary = "주소 수정하기", description = "주소ID를 이용한 주소 수정 / 기본배송지는 해제할 수 없습니다. / 기본배송지는 삭제할 수 없습니다.")
    @PatchMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> updateAddress(@AuthenticationPrincipal CustomPrincipal customPrincipal, @PathVariable Long addressId, @RequestBody AddressDto.Request request) {
        AddressDto.Response response = addressService.updateAddress(customPrincipal.getUserId(), addressId, request);

        ApiResponse<AddressDto.Response> apiResponse = ApiResponse.success("주소 수정 완료", response);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Operation(summary = "주소 삭제하기", description = "주소ID를 이용한 주소 삭제")
    @DeleteMapping("/{addressId}")
    public Long deleteAddress(@AuthenticationPrincipal CustomPrincipal customPrincipal, @PathVariable Long addressId) {
        return addressService.deleteAddress(customPrincipal.getUserId(), addressId);
    }

}
