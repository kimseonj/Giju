package com.bubble.giju.domain.like.entity;

import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="like_id", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "drink_id",nullable = false)
    private Drink drink;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "is_delete", nullable = false)
    private boolean delete;

    private LocalDateTime createdAt;

    public void deleteLike() {
        this.delete = true;
    }

    public void activateLike() {
        this.delete = false;
        this.createdAt = LocalDateTime.now();
    }
}
