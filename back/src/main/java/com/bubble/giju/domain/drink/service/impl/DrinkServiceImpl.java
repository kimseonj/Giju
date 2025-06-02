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
import com.bubble.giju.domain.drink.mapper.DrinkMapper;
import com.bubble.giju.domain.drink.repository.DrinkImageRepository;
import com.bubble.giju.domain.drink.repository.DrinkRepository;
import com.bubble.giju.domain.drink.service.DrinkService;
import com.bubble.giju.domain.image.entity.Image;
import com.bubble.giju.domain.image.repository.ImageRepository;
import com.bubble.giju.domain.image.service.ImageService;
import com.bubble.giju.domain.like.entity.Like;
import com.bubble.giju.domain.like.repository.LikeRepository;
import com.bubble.giju.domain.ranking.enums.Region;
import com.bubble.giju.domain.review.entity.Review;
import com.bubble.giju.domain.review.repository.ReviewRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
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
    private final DrinkMapper drinkMapper;

    @Override
    public DrinkResponseDto saveDrink(DrinkRequestDto drinkRequestDto, List<MultipartFile> files, MultipartFile thumbnail) throws IOException {
        //카테고리 가져옴
        Category category = getCategoryOrThrow(drinkRequestDto.getCategoryId());
        log.debug("카테고리 가져옴");
        String drinkName= generateDrinkName(drinkRequestDto);

        validateDuplicateDrink(drinkName);
        log.debug("상품 이름 확인");
        //술 엔티티 만듦
        Drink drink =toDrinkEntity(drinkRequestDto,category,drinkName);
        drinkRepository.save(drink);
        log.debug("상품저장");
        List<DrinkImage> drinkImages = createDrinkImages(drink, files, thumbnail);
        drinkImageRepository.saveAll(drinkImages);
        log.debug("술 이미지 저장 성공");
        return toDrinkResponseDto(drink, drinkImages);

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
        return buildDrinkResponseDto(drink);
    }

    @Override
    public DrinkDetailResponseDto findById(Long drinkId, UUID userId) {
        Drink drink = drinkRepository.findById(drinkId).orElseThrow(()->new CustomException(ErrorCode.NON_EXISTENT_DRINK));
        //단일 조회이기 때문에 false면 가져오는것이 아닌, 에러를 던지도록 함
        if(drink.is_delete())
        {
            throw new CustomException(ErrorCode.DELETED_DRINK);
        }

        DrinkResponseDto drinkResponseDto = buildDrinkResponseDto(drink);

        long reviewSum =reviewRepository.findSumScoreByDrinkId(drinkId);
        long reviewCount=reviewRepository.countByDrinkId(drinkId);

        double reviewScore = (reviewCount > 0)
                ? (double) reviewSum / reviewCount
                : 0.0;


        Optional<Like> optionalLike = likeRepository.findByUser_UserIdAndDrink_Id(userId, drinkId);
        boolean isLike = optionalLike.filter(like -> !like.isDelete()).isPresent();

        return drinkMapper.toDrinkDetailResponseDto(drinkResponseDto,reviewScore,reviewCount,isLike);
    }

    @Override
    public Page<DrinkDetailResponseDto> findDrinks(String type, String keyword, int pageNum,UUID userUuid) {
        if(type == null || type.isBlank())
        {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_VALUE);
        }

        // 기본 페이지 크기 설정
        int pageSize = 6;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Drink> drinkPage = switch (type) {
            case "category" -> drinkRepository.findByCategoryIdAndDeletedFalse(Integer.parseInt(keyword), pageable);
            case "region" -> drinkRepository.findByRegionAndDeletedFalse(Region.fromName(keyword), pageable);
            case "name" -> {
                if (keyword.isBlank()) {
                    yield drinkRepository.findByDeletedFalseOrderByNameAsc(pageable);
                } else {
                    yield drinkRepository.findByNameContainsAndDeletedFalse(keyword, pageable);
                }
            }
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SEARCH_TYPE);
        };

        List<DrinkDetailResponseDto> dtoList = new ArrayList<>();
        //todo N+1 해결 안했으니까 시간재보고 해결 꼭 할 것 데이터 몇개 없을때 N+1 해결안한 시간 -  94ms
        //술 페이지 1번 + 술마다 (리뷰 3번 + 이미지 2번) -> 5 * 페이지 크기 6 => 31번 조회
        //술 entity graph 사용시 술, 이미지 리뷰 다 가져와서 로직에서 계산하기? -> 1번이면 됨 but..
        //실제 리뷰 데이터를 가져와버리면 서버의 메모리 문제가 생김. query DSL이 가장 적합
        for (Drink drink : drinkPage.getContent()) {
            long reviewSum = reviewRepository.findSumScoreByDrinkId(drink.getId());
            long reviewCount = reviewRepository.countByDrinkId(drink.getId());
            reviewSum = reviewSum < 0 ? 0 : reviewSum;
            reviewCount = reviewCount < 1 ? 1 : reviewCount;

            double reviewScore= (double) reviewSum /reviewCount;

            Optional<Like> optionalLike = likeRepository.findByUser_UserIdAndDrink_Id(userUuid, drink.getId());
            boolean isLike = optionalLike.filter(like -> !like.isDelete()).isPresent();

            DrinkResponseDto dto = buildDrinkResponseDto(drink);
            DrinkDetailResponseDto drinkDetailResponseDto = drinkMapper.toDrinkDetailResponseDto(dto, reviewScore, reviewCount, isLike);

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

    private Category getCategoryOrThrow(int categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NONEXISTENT_CATEGORY));
    }

    private String generateDrinkName(DrinkRequestDto dto) {
        return dto.getName() + " " + dto.getAlcoholContent() + "% " + dto.getVolume() + "mL";
    }

    private void validateDuplicateDrink(String drinkName) {
        if (drinkRepository.existsByName(drinkName)) {
            throw new CustomException(ErrorCode.EXISTENT_DRINK);
        }
    }

    private DrinkResponseDto buildDrinkResponseDto(Drink drink) {
        DrinkImage thumbnailDrinkImage = drinkImageRepository.findByDrinkIdAndThumbnailIsTrue(drink.getId());
        String thumbnailUrl = (thumbnailDrinkImage != null && thumbnailDrinkImage.getImage() != null)
                ? thumbnailDrinkImage.getImage().getUrl()
                : null;

        List<DrinkImage> drinkImageList = drinkImageRepository.findByDrinkIdAndThumbnailIsFalse(drink.getId());
        List<String> imageList = drinkImageList.stream()
                .map(img -> Optional.ofNullable(img.getImage()).map(Image::getUrl).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return drinkMapper.toDrinkResponseDto(drink, thumbnailUrl, imageList);
    }


    private Drink toDrinkEntity(DrinkRequestDto dto, Category category, String drinkName) {
        return Drink.builder()
                .name(drinkName)
                .price(dto.getPrice())
                .stock(dto.getStock())
                .alcoholContent(dto.getAlcoholContent())
                .volume(dto.getVolume())
                .region(Region.fromName(dto.getRegion()))
                .is_delete(false)
                .category(category)
                .build();
    }
    private List<DrinkImage> createDrinkImages(Drink drink, List<MultipartFile> files, MultipartFile thumbnail) throws IOException {
        List<DrinkImage> result = new ArrayList<>();

        String thumbnailUrl = imageService.uploadFile(thumbnail);
        Image thumbnailImage = getImageOrThrow(thumbnailUrl);
        result.add(DrinkImage.builder().drink(drink).image(thumbnailImage).isThumbnail(true).build());

        List<String> urls = imageService.uploadFiles(files);

        List<Image> imageList = imageRepository.findAllByUrlIn(urls);
        if (imageList.size() != urls.size()) {
            log.warn("일부 이미지가 DB에 존재하지 않습니다. 요청한 URL 개수: {}, 실제 조회된 개수: {}", urls.size(), imageList.size());
        }
        for (Image image: imageList) {
            result.add(DrinkImage.builder().drink(drink).image(image).isThumbnail(false).build());
        }

        return result;
    }

    private Image getImageOrThrow(String url) {
        return Optional.ofNullable(imageRepository.findByUrl(url))
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_IMAGE));
    }
    private DrinkResponseDto toDrinkResponseDto(Drink drink, List<DrinkImage> images) {
        String thumbnailUrl = images.stream()
                .filter(DrinkImage::isThumbnail)
                .findFirst()
                .map(di -> di.getImage().getUrl())
                .orElse(null);

        List<String> imageUrls = images.stream()
                .filter(di -> !di.isThumbnail())
                .map(di -> di.getImage().getUrl())
                .collect(Collectors.toList());

        return DrinkResponseDto.builder()
                .id(drink.getId())
                .name(drink.getName())
                .price(drink.getPrice())
                .stock(drink.getStock())
                .alcoholContent(drink.getAlcoholContent())
                .volume(drink.getVolume())
                .is_delete(drink.is_delete())
                .region(String.valueOf((drink.getRegion())))
                .category(new CategoryResponseDto(drink.getCategory().getId(), drink.getCategory().getName()))
                .thumbnailUrl(thumbnailUrl)
                .drinkImageUrlList(imageUrls)
                .build();
    }

}
