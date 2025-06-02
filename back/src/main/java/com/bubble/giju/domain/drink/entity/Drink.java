package com.bubble.giju.domain.drink.entity;

import com.bubble.giju.domain.category.entity.Category;
import com.bubble.giju.domain.drink.dto.DrinkUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table ( name = "Drinks")
public class Drink {

    @Id
    @GeneratedValue
    @Column(name = "drink_id",nullable = false, updatable = false)
    private Long id;

    @Column(name = "drink_name",nullable = false, length = 50,unique = true)
    private String name;

    @Column(name = "drink_price",nullable = false)
    private int price;

    @Column(name = "drink_stock",nullable = false)
    private int stock;
    @Column(name = "drink_alcohol_content",nullable = false)
    private double alcoholContent;
    @Column(name = "drink_volume",nullable = false)
    private int volume;
    @Column(name = "drink_is_delete",nullable = false)
    private boolean is_delete;
    @Column(name = "drink_region",nullable = false,length = 10)
    private String region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id",nullable = false)
    private Category category;

    @Builder
    public Drink (Long id ,String name,int price,int stock,double alcoholContent,int volume,boolean is_delete,String region,Category category){
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.alcoholContent = alcoholContent;
        this.volume = volume;
        this.is_delete = is_delete;
        this.region = region;
        this.category = category;
    }

    public boolean is_delete()
    {
        return this.is_delete;
    }

    public void updateDelete(boolean is_delete)
    {
        this.is_delete = is_delete;
    }

    public void update(DrinkUpdateRequestDto dto, Category category) {
        this.price = dto.getPrice();
        this.stock = dto.getStock();
        this.region = dto.getRegion();
        this.category = category;
    }


}
