package com.bubble.giju.domain.category.service.impl;

import com.bubble.giju.domain.category.dto.CategoryResponseDto;
import com.bubble.giju.domain.category.repository.CategoryRepository;
import com.bubble.giju.domain.category.service.CategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    public final CategoryRepository categoryRepository;


    /*
    * 카테고리 리스트(전체)를 불러오는 메서드
    * */
    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC,"id")).stream().map(
                category -> new CategoryResponseDto(category.getId(), category.getName()))
                .toList();
    }
}
