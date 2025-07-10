package com.bubble.giju.domain.drink.controller;

import com.bubble.giju.domain.category.dto.CategoryResponseDto;
import com.bubble.giju.domain.drink.dto.*;
import com.bubble.giju.domain.drink.service.DrinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DrinkController.class)
@AutoConfigureMockMvc(addFilters = false)
class DrinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DrinkService drinkService;

    private final CategoryResponseDto category = new CategoryResponseDto(1, "탁주");

    private DrinkResponseDto createSampleResponseDto() {
        return DrinkResponseDto.builder()
                .id(1L)
                .name("서울막걸리")
                .price(5000)
                .stock(50)
                .alcoholContent(6.0)
                .volume(750)
                .is_delete(false)
                .region("서울")
                .category(category)
                .thumbnailUrl("thumb.jpg")
                .drinkImageUrlList(List.of("img1.jpg", "img2.jpg"))
                .build();
    }

    private DrinkDetailResponseDto createSampleDetailDto() {
        return DrinkDetailResponseDto.builder()
                .id(1L)
                .name("서울막걸리")
                .price(5000)
                .stock(50)
                .alcoholContent(6.0)
                .volume(750)
                .is_delete(false)
                .region("서울")
                .category(category)
                .thumbnailUrl("thumb.jpg")
                .drinkImageUrlList(List.of("img1.jpg", "img2.jpg"))
                .reviewScore(4.5)
                .reviewCount(12)
                .is_like(true)
                .build();
    }

    @Test
    @DisplayName("상품(술) 등록 성공 테스트")
    void addDrink() throws Exception {
        DrinkRequestDto request = DrinkRequestDto.builder()
                .name("서울막걸리")
                .price(5000)
                .stock(50)
                .alcoholContent(6.0)
                .volume(750)
                .region("서울")
                .categoryId(1)
                .build();

        DrinkResponseDto response = createSampleResponseDto();

        MockMultipartFile drinkJson = new MockMultipartFile("drink", "", "application/json",
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile file = new MockMultipartFile("files", "img1.jpg", "image/jpeg", "fake-image".getBytes());
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", "thumb-img".getBytes());

        Mockito.when(drinkService.saveDrink(any(), any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/admin/drink")
                        .file(drinkJson)
                        .file(file)
                        .file(thumbnail)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("술 상품 등록에 성공하셨습니다."))
                .andExpect(jsonPath("$.data.name").value("서울막걸리"));
    }

    @Test
    @DisplayName("상품(술) 삭제 성공 테스트")
    void deleteDrink() throws Exception {
        Mockito.when(drinkService.deleteDrink(1L)).thenReturn(createSampleResponseDto());

        mockMvc.perform(delete("/api/admin/drink/{drinkId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("술 상품 삭제에 성공하셨습니다."))
                .andExpect(jsonPath("$.data.name").value("서울막걸리"));
    }

    @Test
    @DisplayName("상품(술) 복구 성공 테스트")
    void restoreDrink() throws Exception {
        Mockito.when(drinkService.restoreDrink(1L)).thenReturn(createSampleResponseDto());

        mockMvc.perform(patch("/api/admin/drink/{drinkId}/restore", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("술 상품 복원에 성공하셨습니다."))
                .andExpect(jsonPath("$.data.name").value("서울막걸리"));
    }

    @Test
    @DisplayName("상품(술) 정보 수정 성공 테스트")
    void updateDrink() throws Exception {
        DrinkUpdateRequestDto request = DrinkUpdateRequestDto.builder()
                .price(6000)
                .stock(40)
                .region("경기")
                .categoryId(1)
                .build();

        Mockito.when(drinkService.updateDrink(eq(1L), any())).thenReturn(createSampleResponseDto());

        mockMvc.perform(patch("/api/admin/drink/{drinkId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("술 상품 업데이트에 성공하셨습니다."))
                .andExpect(jsonPath("$.data.name").value("서울막걸리"));
    }

    @Test
    @DisplayName("상품(술) 단일조회 성공 테스트")
    void findDrink() throws Exception {
        Mockito.when(drinkService.findById(eq(1L), any())).thenReturn(createSampleDetailDto());

        mockMvc.perform(get("/api/drink/{drinkId}", 1L)
                        .principal(() -> UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("서울막걸리"))
                .andExpect(jsonPath("$.reviewScore").value(4.5));
    }

    @Test
    @DisplayName("상품(술) 검색 성공 테스트")
    void findDrinkList() throws Exception {
        DrinkDetailResponseDto detail = createSampleDetailDto();

        Mockito.when(drinkService.findDrinks(eq("category"), eq("1"), eq(0), any()))
                .thenReturn(new PageImpl<>(List.of(detail), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/drinks")
                        .param("type", "category")
                        .param("keyword", "1")
                        .param("pageNum", "1")
                        .principal(() -> UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("서울막걸리"));
    }
}
