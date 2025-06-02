package com.bubble.giju.domain.drink.controller;

import com.bubble.giju.domain.drink.dto.DrinkDetailResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkRequestDto;
import com.bubble.giju.domain.drink.dto.DrinkResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkUpdateRequestDto;
import com.bubble.giju.domain.drink.service.DrinkService;
import com.bubble.giju.domain.user.dto.CustomPrincipal;
import com.bubble.giju.global.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Drink",description = "술 관련 API")
public class DrinkController {

    private final DrinkService drinkService;

    //todo admin 관련 api가 존재하지않아 임시로 만듦 Value 어노테이션 통해서 검증도 해야함.
    //todo ApiResponse 성공은 모두 200 처리? 근데 객체안에 들어있긴함.
    @PostMapping(value = "/api/admin/drink", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "술(상품)등록",description = "술(상품) 을 등록하는 API")
    public ResponseEntity<ApiResponse<DrinkResponseDto>> addDrink(
            @RequestPart("drink") DrinkRequestDto drinkDto,   // JSON 데이터를 part로 받음
            @RequestPart("files") List<MultipartFile> files,  // 여러 파일
            @RequestPart("thumbnail") MultipartFile thumbnail // 단일 파일
            ) throws IOException {

        DrinkResponseDto drinkResponseDto = drinkService.saveDrink(drinkDto,files,thumbnail);
        ApiResponse<DrinkResponseDto> apiResponse=ApiResponse.success("술 상품 등록에 성공하셨습니다.",drinkResponseDto);
        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }

    @Operation(summary = "술(상품)삭제",description = "술(상품) 을 삭제하는 API")
    @DeleteMapping(value = "/api/admin/drink/{drinkId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DrinkResponseDto>> deleteDrink(@PathVariable(name = "drinkId") Long drinkId
    ) throws IOException {
        DrinkResponseDto drinkResponseDto = drinkService.deleteDrink(drinkId);
        ApiResponse<DrinkResponseDto> apiResponse=ApiResponse.success("술 상품 삭제에 성공하셨습니다.",drinkResponseDto);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @Operation(summary = "삭제된 술(상품) 재판매",description = "삭제된 술(상품)을 다시 판매할 때 사용하는 API")
    @PatchMapping("/api/admin/drink/{drinkId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DrinkResponseDto>> restoreDrink(@PathVariable(name = "drinkId") Long drinkId
    ) throws IOException {
        DrinkResponseDto drinkResponseDto = drinkService.restoreDrink(drinkId);
        ApiResponse<DrinkResponseDto> apiResponse=ApiResponse.success("술 상품 복원에 성공하셨습니다.",drinkResponseDto);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @Operation(summary = "술(상품) 업데이트",description = "술(상품) 업데이트 API")
    @PatchMapping("/api/admin/drink/{drinkId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DrinkResponseDto>> UpdateDrink(@PathVariable Long drinkId,
                                                                     @RequestBody DrinkUpdateRequestDto drinkUpdateRequestDto
                                                                     ) throws IOException {
        DrinkResponseDto drinkResponseDto = drinkService.updateDrink(drinkId,drinkUpdateRequestDto);
        ApiResponse<DrinkResponseDto> apiResponse=ApiResponse.success("술 상품 업데이트에 성공하셨습니다.",drinkResponseDto);
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @Operation(summary = "술(상품) 단일조회",description = "술(상품) 단일조회 API")
    @GetMapping("/api/drink/{drinkId}")
    public ResponseEntity<DrinkDetailResponseDto> findDrink(@PathVariable Long drinkId,
                                                                         @AuthenticationPrincipal CustomPrincipal userDetails  // 인증 안 된 경우 null
    ) throws IOException {
        String userId = (userDetails != null ? userDetails.getUserId() : null);
        UUID userUuid = userId == null ? null: UUID.fromString(userId);
        DrinkDetailResponseDto drinkDetailResponseDto = drinkService.findById(drinkId,userUuid);
        return ResponseEntity.ok(drinkDetailResponseDto);
    }


    @Operation(summary = "술(상품) 검색",description = "술(상품) 검색 API")
    @GetMapping("/api/drinks")
    public ResponseEntity<Page<DrinkDetailResponseDto>> findDrinkList(
            @Parameter(description = "category(카테고리) , region(지역) , name(이름으로 검색)", required = true)
            @RequestParam(required = true) String type,
            @Parameter(description = "검색하고자 하는 키워드. ex) category: 카테고리의 아이디 값 (1), region : 지역 이름(서울) , name : 검색하고자 하는 술의 이름(막걸리), category와 region은 정확한 값을 가져와야하고, name은 Like연산을 통해 가져옵니다.", required = true)
            @RequestParam(required = true) String keyword,
            @Parameter(description = "페이지 번호")
            @RequestParam(required = false,defaultValue = "1") int pageNum,
                                                            @AuthenticationPrincipal CustomPrincipal userDetails  // 인증 안 된 경우 null
    ) throws IOException {
        if(pageNum<1)
        {
            pageNum=1;
        }
        String userId = (userDetails != null ? userDetails.getUserId() : null);
        UUID userUuid = userId == null ? null: UUID.fromString(userId);
        Page<DrinkDetailResponseDto> drinkResponseDtoPage = drinkService.findDrinks(type,keyword,pageNum-1,userUuid);
        return ResponseEntity.ok(drinkResponseDtoPage);
    }

}
