package com.bubble.giju.domain.drink.repository.impl;

import com.bubble.giju.domain.category.entity.QCategory;
import com.bubble.giju.domain.drink.entity.Drink;
import com.bubble.giju.domain.drink.entity.QDrink;
import com.bubble.giju.domain.drink.entity.QDrinkImage;
import com.bubble.giju.domain.drink.repository.DrinkRepositoryCustom;
import com.bubble.giju.domain.image.entity.QImage;
import com.bubble.giju.domain.like.entity.QLike;
import com.bubble.giju.domain.ranking.enums.Region;
import com.bubble.giju.domain.review.entity.QReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class DrinkRepositoryCustomImpl extends QuerydslRepositorySupport implements DrinkRepositoryCustom {

    public DrinkRepositoryCustomImpl() {
        super(Drink.class);
    }
    @Override
    public Page<Tuple> findDrinksCustom(String type,String keyword,UUID userId, Pageable pageable) {
        QDrink d = QDrink.drink;
        QDrinkImage di = QDrinkImage.drinkImage;
        QImage i = QImage.image;
        QLike l = QLike.like;
        QReview r = QReview.review;
        QCategory c = QCategory.category;

        Expression<?> isLikedExpr = (userId != null)
                ? ExpressionUtils.as(l.id.max().isNotNull().and(l.delete.eq(false)), "isLiked")
                : ExpressionUtils.as(Expressions.constant(false), "isLiked");

        JPQLQuery<Tuple> query = getQuerydsl().createQuery()
                .select(
                        d.id,
                        d.name,
                        d.price,
                        d.stock,
                        d.alcoholContent,
                        d.volume,
                        d.deleted,
                        d.region,
                        c.id.as("categoryId"),
                        c.name.as("categoryName"),
                        ExpressionUtils.as(
                                Expressions.stringTemplate("GROUP_CONCAT({0})", i.url),
                                "imageUrls"
                        ),
                        ExpressionUtils.as(
                                Expressions.stringTemplate("MAX(CASE WHEN {0} = TRUE THEN {1} ELSE NULL END)",
                                        di.thumbnail,
                                        i.url),
                                "thumbnailUrl"
                        ),
                        isLikedExpr,
                        ExpressionUtils.as(r.id.countDistinct(), "reviewCount"),
                        ExpressionUtils.as(r.score.avg().coalesce(0.0), "avgScore")
                )
                .from(d)
                .leftJoin(di).on(di.drink.eq(d))
                .leftJoin(i).on(di.image.eq(i))
                .leftJoin(r).on(r.drink.eq(d))
                .leftJoin(c).on(d.category.eq(c));

        if (userId != null) {
            query.leftJoin(l).on(l.drink.eq(d).and(l.user.userId.eq(userId)));
        }

        BooleanBuilder where = new BooleanBuilder();
        where.and(d.deleted.eq(false));

        if (type != null && type.equals("category")) {
            where.and(d.category.id.eq(Integer.parseInt(keyword)));
        }
        if (type != null && type.equals("region")) {
            where.and(d.region.eq(Region.fromName(keyword)));
        }
        if (type != null  && type.equals("name")) {
            where.and(d.name.like("%" + keyword + "%"));
        }

        query.where(where)
                .groupBy(d.id, d.name, d.price, d.stock, d.alcoholContent, d.volume,
                        d.deleted, d.region, c.id, c.name)
                .orderBy(d.id.asc());

        long total = query.fetchCount();
        JPQLQuery<Tuple> pagedQuery = getQuerydsl().applyPagination(pageable, query);
        List<Tuple> results = pagedQuery.fetch();

        return new PageImpl<>(results, pageable, total);
    }

}
