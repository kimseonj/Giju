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
import com.bubble.giju.domain.drink.entity.QDrink;
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
import com.bubble.giju.domain.review.repository.ReviewRepository;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

    /*
    * 상품(술) 저장하는 메서드
    * */
    @Override
    public DrinkResponseDto saveDrink(DrinkRequestDto drinkRequestDto, List<MultipartFile> files, MultipartFile thumbnail) throws IOException {
        //카테고리 가져옴
        Category category = getCategoryOrThrow(drinkRequestDto.getCategoryId());
        String drinkName= generateDrinkName(drinkRequestDto);

        validateDuplicateDrink(drinkName);
        //술 엔티티 만듦
        Drink drink =toDrinkEntity(drinkRequestDto,category,drinkName);
        drinkRepository.save(drink);
        List<DrinkImage> drinkImages = createDrinkImages(drink, files, thumbnail);
        drinkImageRepository.saveAll(drinkImages);
        return toDrinkResponseDto(drink, drinkImages);

    }
    /*
     * 상품(술) 삭제하는 메서드
     * */
    //todo  @Where + @SQLDelete로 자동 처리??
    @Override
    public DrinkResponseDto deleteDrink(Long drinkId) {
        return updateDrinkDeleteStatus(drinkId, true);
    }
    /*
     * 상품(술) 데이터 되살리는 메서드
     * */
    @Override
    public DrinkResponseDto restoreDrink(Long drinkId) {
        return updateDrinkDeleteStatus(drinkId, false);
    }
    /*
     * 상품(술) 정보 수정하는 메서드
     * */
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
    /*
     * 상품(술) 단일 조회 메서드
     * */
    @Override
    public DrinkDetailResponseDto findById(Long drinkId, UUID userId) {
        Drink drink = drinkRepository.findById(drinkId).orElseThrow(()->new CustomException(ErrorCode.NON_EXISTENT_DRINK));
        //단일 조회이기 때문에 false면 가져오는것이 아닌, 에러를 던지도록 함
        if(drink.is_delete())
        {
            log.warn("삭제한 상품(술) 정보를 요청했습니다.");
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
    /*
     * 상품(술) 검색 메서드
     * type : category(카테고리), region(지역) , name(이름)
     * keyword : 검색하고자 하는 키워드
     * - category : 1 , 2 등 category Id 를 기준으로 한 값
     * - region : String 으로 지역 기준으로 한 값이지만 Enum으로 존재하지 않는 값이라면 오류를 냄
     * - name : String 으로 Like 연산을 통해 조회하게됨
     * JPA를 이용하여 조회 5N +1 문제 : 94ms , 160ms ->queryDSL -> 3ms,2ms
     * 양뱡향 통해 리뷰 가져오면 서버 메모리 부하
     * */
    @Override
    public Page<DrinkDetailResponseDto> findDrinks(String type, String keyword, int pageNum,UUID userId) {
        if(type == null || type.isBlank())
        {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_VALUE);
        }

        // 기본 페이지 크기 설정
        int pageSize = 6;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Tuple> drinkPage = drinkRepository.findDrinksCustom(type,keyword,userId,pageable);

        List<DrinkDetailResponseDto> drinkDetailResponseDtoList = new ArrayList<>();

        for(Tuple tuple : drinkPage)
        {
            String imageUrls = tuple.get(Expressions.stringPath("imageUrls"));
            assert imageUrls != null;
            List<String> imageUrlList ;
            if (imageUrls.isEmpty()) {
                imageUrlList= Collections.emptyList();
            }
           else imageUrlList = Arrays.stream(imageUrls.split(",")).toList();

            boolean isLike;

            if(userId!=null)
            {
                isLike = Optional.ofNullable(tuple.get(Expressions.booleanPath("isLiked"))).orElse(false);
            }
            else
            {
                isLike=false;
            }

            DrinkDetailResponseDto dto = DrinkDetailResponseDto.builder()
                    .id(tuple.get(QDrink.drink.id))
                    .name(tuple.get(QDrink.drink.name))
                    .price(Optional.ofNullable(tuple.get(QDrink.drink.price)).orElse(0))
                    .stock(Optional.ofNullable(tuple.get(QDrink.drink.stock)).orElse(0))
                    .alcoholContent(Optional.ofNullable(tuple.get(QDrink.drink.alcoholContent)).orElse(0.0))
                    .volume(Optional.ofNullable(tuple.get(QDrink.drink.volume)).orElse(0))
                    .is_delete(Optional.ofNullable(tuple.get(QDrink.drink.deleted)).orElse(false))
                    .region(Optional.ofNullable(tuple.get(QDrink.drink.region)).map(Enum::toString).orElse("UNKNOWN"))
                    .category(new CategoryResponseDto(
                            Optional.ofNullable(tuple.get(Expressions.numberPath(Integer.class, "categoryId"))).orElse(0),
                            Optional.ofNullable(tuple.get(Expressions.stringPath("categoryName"))).orElse("알 수 없음")
                    ))
                    .thumbnailUrl(tuple.get(Expressions.stringPath("thumbnailUrl")))
                    .drinkImageUrlList(imageUrlList)
                    .reviewScore(Optional.ofNullable(tuple.get(Expressions.numberPath(Double.class, "avgScore"))).orElse(0.0))
                    .reviewCount(Optional.ofNullable(tuple.get(Expressions.numberPath(Long.class, "reviewCount"))).orElse(0L))
                    .is_like(isLike)
                    .build();

            drinkDetailResponseDtoList.add(dto);
        }

        return new PageImpl<>(drinkDetailResponseDtoList, pageable, drinkPage.getTotalElements());
    }
    /*
     * 상품(술) 삭제 메서드
     * */
    private DrinkResponseDto updateDrinkDeleteStatus(Long drinkId, boolean isDeleted) {
        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_DRINK));

        drink.updateDelete(isDeleted);
        drinkRepository.save(drink);

        return buildDrinkResponseDto(drink);
    }
    /*
     * 카테고리 존재하는지 확인하는 메서드
     * */
    private Category getCategoryOrThrow(int categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NONEXISTENT_CATEGORY));
    }
    /*
     * 상품(술) 이름 생성 메서드
     * */
    private String generateDrinkName(DrinkRequestDto dto) {
        return dto.getName() + " " + dto.getAlcoholContent() + "% " + dto.getVolume() + "mL";
    }
    /*
     * 이미 존재하는 상품(술)인지 확인하는 메서드
     * */
    private void validateDuplicateDrink(String drinkName) {
        if (drinkRepository.existsByName(drinkName)) {
            throw new CustomException(ErrorCode.EXISTENT_DRINK);
        }
    }
    /*
     * 상품(술) DTO 변환 메서드
     * */
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

    /*
     * 상품(술) Entity 변환 메서드
     * */
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
    /*
    * 이미지 생성 및 저장 메서드
    * */
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
    /*
    * 이미 존재하는 url인지 확인하는 메서드
    * */
    private Image getImageOrThrow(String url) {
        return Optional.ofNullable(imageRepository.findByUrl(url))
                .orElseThrow(() -> new CustomException(ErrorCode.NON_EXISTENT_IMAGE));
    }
    /*
    * 상품(술) DTO 변환 메서드
    * */
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
