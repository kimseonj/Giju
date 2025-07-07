package com.bubble.giju.domain.category.controller;

import com.bubble.giju.domain.category.dto.CategoryResponseDto;
import com.bubble.giju.domain.category.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 무시
class CategoryControllerTest {

    @Autowired
    private CategoryController categoryController;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    List<CategoryResponseDto> mockCategories;

    @BeforeEach
    public void setUp() {
        mockCategories = new ArrayList<>(Arrays.asList(
                new CategoryResponseDto(1,"탁주"),new CategoryResponseDto(2,"청주"),
                new CategoryResponseDto(3,"증주"),new CategoryResponseDto(4,"약주"),
                new CategoryResponseDto(5,"과실주"),new CategoryResponseDto(6,"기타")));
    }

    @Test
    void getCategory() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(mockCategories);

        mockMvc.perform(get("/api/categories")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(mockCategories.size()));


    }
}