package com.bubble.giju.global.runner;

import com.bubble.giju.domain.category.entity.Category;
import com.bubble.giju.domain.category.repository.CategoryRepository;
import com.bubble.giju.domain.delivery.entity.DeliveryCompany;
import com.bubble.giju.domain.delivery.repository.DeliveryCompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final DeliveryCompanyRepository deliveryCompanyRepository;

    private final List<String> categories=new ArrayList<>(Arrays.asList("탁주","청주","증류주","약주","과실주","기타"));
    private final List<String> companies= new ArrayList<>(Arrays.asList("CJ대한통운","우체국택배"));

    public DataInitializer(CategoryRepository categoryRepository, DeliveryCompanyRepository deliveryCompanyRepository) {
        this.categoryRepository = categoryRepository;
        this.deliveryCompanyRepository = deliveryCompanyRepository;
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

        List<DeliveryCompany> deliveryCompanyList=new ArrayList<DeliveryCompany>();

        for(String companyName:companies){
            if(!deliveryCompanyRepository.existsByName(companyName))
            {
                deliveryCompanyList.add(new DeliveryCompany(companyName));
            }
        }
        if (!deliveryCompanyList.isEmpty()) {
            deliveryCompanyRepository.saveAll(deliveryCompanyList);
        }

    }
}
