package com.bubble.giju.domain.drink.service.impl;

import com.bubble.giju.domain.category.dto.CategoryResponseDto;
import com.bubble.giju.domain.category.entity.Category;
import com.bubble.giju.domain.category.repository.CategoryRepository;
import com.bubble.giju.domain.drink.dto.DrinkDetailResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkRequestDto;
import com.bubble.giju.domain.drink.dto.DrinkResponseDto;
import com.bubble.giju.domain.drink.dto.DrinkUpdateRequestDto;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.entity.DrinkImage;
import com.bubble.giju.domain.drink.repository.DrinkImageRepository;
import com.bubble.giju.domain.drink.repository.DrinkRepository;
import com.bubble.giju.domain.drink.service.DrinkService;
import com.bubble.giju.domain.image.entity.Image;
import com.bubble.giju.domain.image.repository.ImageRepository;
import com.bubble.giju.domain.image.service.ImageService;
import com.bubble.giju.domain.like.entity.Like;
import com.bubble.giju.domain.like.repository.LikeRepository;
import com.bubble.giju.domain.review.entity.Review;
import com.bubble.giju.domain.review.repository.ReviewRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DrinkServiceImpl implements DrinkService {
    private final ImageService imageService;
    private final DrinkRepository drinkRepository;
    private final CategoryRepository categoryRepository;
    private final DrinkImageRepository drinkImageRepository;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;

    //todo 예외처리 해줄 것 Getter 다 삼항연산자로 null 넣도록 하자.
    @Override
    public DrinkResponseDto saveDrink(DrinkRequestDto drinkRequestDto, List<MultipartFile> files, MultipartFile thumbnail) throws IOException {
        //카테고리 가져옴
        Category category = categoryRepository.findById(drinkRequestDto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.NONEXISTENT_CATEGORY));

        String drinkName= drinkRequestDto.getName()+" "+drinkRequestDto.getAlcoholContent()+"% "+drinkRequestDto.getVolume()+"mL";

        //술 엔티티 만듦
        Drink drink =Drink.builder().name(drinkName).price(drinkRequestDto.getPrice())
                .stock(drinkRequestDto.getStock()).alcoholContent(drinkRequestDto.getAlcoholContent())
                .volume(drinkRequestDto.getVolume()).is_delete(false).region(drinkRequestDto.getRegion())
                .category(category).build();
        //썸네일 이미지 생성 및 이미지 저장
        String thumbnailUrl=imageService.uploadFile(thumbnail);
        //todo 이거 안전하게 레파지토리에서 가져오는지? 아니면 그냥 사용해도되는지? 이대로면 예외처리도 추가해야함.
        Image thumbnailImage= imageRepository.findByUrl(thumbnailUrl);

        //썸네일 술_이미지 엔티티생성
        List<DrinkImage> drinkImageList = new ArrayList<>();
        DrinkImage thumbnailDrinkImage = DrinkImage.builder().drink(drink).image(thumbnailImage).isThumbnail(true).build();
        drinkImageList.add(thumbnailDrinkImage);

        //다른 이미지들 저장 및 url 반환받음
        List<String> drinkImageUrlList = imageService.uploadFiles(files);
        //다른 이미지들 술_이미지 테이블에 저장
        for(String drinkImageUrl:drinkImageUrlList){
            //todo N+1 문제 와 함께 예외처리
            Image image = imageRepository.findByUrl(drinkImageUrl);
            DrinkImage drinkImage= DrinkImage.builder().drink(drink).image(image).isThumbnail(false).build();
            drinkImageList.add(drinkImage);
        }
        drinkRepository.save(drink);
        drinkImageRepository.saveAll(drinkImageList);

        DrinkResponseDto drinkResponseDto = DrinkResponseDto.builder().id(drink.getId())
                .name(drink.getName()).price(drink.getPrice()).stock(drink.getStock())
                .alcoholContent(drink.getAlcoholContent()).volume(drink.getVolume())
                .is_delete(drink.is_delete()).region(drink.getRegion()).category(new CategoryResponseDto(drink.getCategory().getId(),drink.getCategory().getName()))
                .thumbnailUrl(thumbnailUrl).drinkImageUrlList(drinkImageUrlList)
                .build();
        return drinkResponseDto;
    }

    //todo  @Where + @SQLDelete로 자동 처리??
    @Override
    public DrinkResponseDto deleteDrink(Long drinkId) {
        return updateDrinkDeleteStatus(drinkId, true);
    }

    @Override
    public DrinkResponseDto restoreDrink(Long drinkId) {
        return updateDrinkDeleteStatus(drinkId, false);
    }

    @Override
    public DrinkResponseDto updateDrink(Long drinkId,DrinkUpdateRequestDto drinkUpdateRequestDto) {
        Drink drink = drinkRepository.findById(drinkId).orElseThrow(()-> new CustomException(ErrorCode.NON_EXISTENT_DRINK));
        Category category = categoryRepository.findById(drinkUpdateRequestDto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.NONEXISTENT_CATEGORY));
        drink.update(drinkUpdateRequestDto,category);
        //커밋시에 자동 변경감지 되지만 명시적으로 적어줌
        drink = drinkRepository.save(drink);
        DrinkResponseDto drinkResponseDto = buildDrinkResponseDto(drink);
        return drinkResponseDto;
    }

    @Override
    public DrinkDetailResponseDto findById(Long drinkId, UUID userId) {
        Drink drink = drinkRepository.findById(drinkId).orElseThrow(()->new CustomException(ErrorCode.NON_EXISTENT_DRINK));

        DrinkResponseDto drinkResponseDto = buildDrinkResponseDto(drink);

        long reviewSum =reviewRepository.findSumScoreByDrinkId(drinkId);
        long reviewCount=reviewRepository.countByDrinkId(drinkId);

        double reviewScore = (reviewCount > 0)
                ? (double) reviewSum / reviewCount
                : 0.0;


        boolean isLike= likeRepository.existsByUser_UserIdAndDrink_id(userId,drinkId);

        //todo Mapper 이용할것
        DrinkDetailResponseDto drinkDetailResponseDto = DrinkDetailResponseDto.builder()
                .id(drinkResponseDto.getId()).name(drinkResponseDto.getName()).price(drinkResponseDto.getPrice())
                .stock(drinkResponseDto.getStock()).alcoholContent(drink.getAlcoholContent())
                .volume(drinkResponseDto.getVolume()).is_delete(drinkResponseDto.is_delete())
                .region(drinkResponseDto.getRegion())
                .category(new CategoryResponseDto(drinkResponseDto.getCategory().getId(),drinkResponseDto.getCategory().getName()))
                .thumbnailUrl(drinkResponseDto.getThumbnailUrl()).drinkImageUrlList(drinkResponseDto.getDrinkImageUrlList())
                .reviewScore(reviewScore).reviewCount(reviewCount)
                .is_like(isLike).build();

        return drinkDetailResponseDto;
    }

    @Override
    public Page<DrinkDetailResponseDto> findDrinks(String type, String keyword, int pageNum,UUID userUuid) {
        if(type == null || type.isBlank()|| keyword==null || keyword.isBlank())
        {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_VALUE);
        }

        // 기본 페이지 크기 설정
        int pageSize = 6;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Drink> drinkPage=null;

        if(type.equals("category"))
        {
            drinkPage= drinkRepository.findByCategoryIdIAndIs_deleteFalse(Integer.parseInt(keyword),pageable);
        }
        else if(type.equals("region"))
        {
            drinkPage= drinkRepository.findByRegionIsDeleteFalse(keyword,pageable);
        }
        else if(type.equals("name"))
        {
            drinkPage=drinkRepository.findByNameContainsIAndIs_deleteFalse(keyword,pageable);
        }
        else
        {
            throw new CustomException(ErrorCode.UNSUPPORTED_SEARCH_TYPE);
        }
        List<DrinkDetailResponseDto> dtoList = new ArrayList<>();
        //todo N+1 해결 안했으니까 시간재보고 해결 꼭 할 것 데이터 몇개 없을때 N+1 해결안한 시간 -  94ms
        for (Drink drink : drinkPage.getContent()) {
            long reviewSum = reviewRepository.findSumScoreByDrinkId(drink.getId());
            long reviewCount = reviewRepository.countByDrinkId(drink.getId());
            reviewSum = reviewSum < 0 ? reviewSum : 0;
            reviewCount = reviewCount < 0 ? reviewCount : 1;
            double reviewScore= (double) reviewSum /reviewCount;

            boolean is_like = likeRepository.existsByUser_UserIdAndDrink_id(userUuid,drink.getId());

            DrinkResponseDto dto = buildDrinkResponseDto(drink); // 이 메서드 구현 필요
            DrinkDetailResponseDto drinkDetailResponseDto= DrinkDetailResponseDto.from(dto,reviewSum,reviewCount,is_like);
            dtoList.add(drinkDetailResponseDto);
        }
        return new PageImpl<>(dtoList, pageable, drinkPage.getTotalElements());
    }

    private DrinkResponseDto updateDrinkDeleteStatus(Long drinkId, boolean isDeleted) {
        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_DRINK));

        drink.updateDelete(isDeleted);
        drinkRepository.save(drink);

        return buildDrinkResponseDto(drink);
    }

    private DrinkResponseDto buildDrinkResponseDto(Drink drink) {
        // 썸네일 처리
        DrinkImage thumbnailDrinkImage = drinkImageRepository.findByDrinkIdAndThumbnailIsTrue(drink.getId());
        String thumbnailUrl = thumbnailDrinkImage != null && thumbnailDrinkImage.getImage() != null
                ? thumbnailDrinkImage.getImage().getUrl()
                : null;

        // 일반 이미지 리스트 처리
        List<DrinkImage> drinkImageList = Optional.ofNullable(
                drinkImageRepository.findByDrinkIdAndThumbnailIsFalse(drink.getId())
        ).orElse(Collections.emptyList());

        List<String> imageList = drinkImageList.stream()
                .map(img -> img.getImage() != null ? img.getImage().getUrl() : null)
                .filter(Objects::nonNull)
                .toList();

        return DrinkResponseDto.builder()
                .id(drink.getId())
                .name(drink.getName())
                .price(drink.getPrice())
                .stock(drink.getStock())
                .alcoholContent(drink.getAlcoholContent())
                .volume(drink.getVolume())
                .is_delete(drink.is_delete())
                .region(drink.getRegion())
                .category(new CategoryResponseDto(drink.getCategory().getId(), drink.getCategory().getName()))
                .thumbnailUrl(thumbnailUrl)
                .drinkImageUrlList(imageList)
                .build();
    }


}
