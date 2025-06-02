package com.bubble.giju.domain.image.repository;

import com.bubble.giju.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findByUrl(String name);
}
