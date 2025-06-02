package com.bubble.giju.global.runner;

import com.bubble.giju.domain.category.entity.Category;
import com.bubble.giju.domain.category.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    private final List<String> categories=new ArrayList<>(Arrays.asList("탁주","청주","증류주","약주","과실주","기타"));
    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        List<Category> categoryList=new ArrayList<Category>();

        for(String categoryName:categories){
            if(!categoryRepository.existsByName(categoryName)){
                categoryList.add(new Category(categoryName));
            }
        }

        if (!categoryList.isEmpty()) {
            categoryRepository.saveAll(categoryList);
        }
    }
}
