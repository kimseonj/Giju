package com.bubble.giju.domain.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="Images")
@Getter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="image_id", nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(name="image_url",length=255,nullable=false, unique=true)
    private String url;

    @Builder
    public Image(String url) {
        this.url = url;
    }
}
