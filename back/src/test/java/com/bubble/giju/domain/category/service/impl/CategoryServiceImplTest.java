package com.bubble.giju.domain.category.service.impl;

import com.bubble.giju.domain.category.dto.CategoryResponseDto;
import com.bubble.giju.domain.category.entity.Category;
import com.bubble.giju.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;
    @DisplayName("카테고리 리스트 조회 성공 테스트")
    @Test
    void getAllCategories() {
        //given
        List<Category> mockCategories = new ArrayList<>(Arrays.asList(
                new Category("탁주"),new Category("청주"),
                new Category("증주"),new Category("약주"),
                new Category("과실주"),new Category("기타")));

        //when
        when(categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))).thenReturn(mockCategories);

        List<CategoryResponseDto> categoryResponseDtoList=categoryService.getAllCategories();
        assertNotNull(categoryResponseDtoList);
        assertEquals(6, categoryResponseDtoList.size());

    }
}