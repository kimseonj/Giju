package com.bubble.giju.domain.review.entity;

import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.image.entity.Image;
import com.bubble.giju.domain.order.entity.Order;
import com.bubble.giju.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "drink_id",nullable = false)
    private Drink drink;

    @ManyToOne
    @JoinColumn(name="order_id",nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name="image_id")
    private Image image;

    // CLOB/TEXT 타입으로 매핑
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private int score;

    @Builder
    public Review(User user, Drink drink, Order order, Image image, String content, int score) {
        this.user = user;
        this.drink = drink;
        this.order = order;
        this.image = image;
        this.content = content;
        this.score = score;
    }
}
