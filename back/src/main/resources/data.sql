INSERT INTO users (user_id, login_id, password, name, email, phone_number, birthday, created_at, role)
VALUES ('11111111-1111-1111-1111-111111111112', 'testuser1', '$2a$12$WbQzovwzqOtzDEUBjGeFcehv9OWx/ZJ0E0V3UHMJRw3m8Uiq..mDi', '김선준', 'kim@example.com', '01012348678', '1999-01-01', '2025-01-01T10:00:00', 'USER');

INSERT INTO users (user_id, login_id, password, name, email, phone_number, birthday, created_at, role)
VALUES ('22222222-2222-2222-2222-222222222222', 'testuser2', '$2a$12$WbQzovwzqOtzDEUBjGeFcehv9OWx/ZJ0E0V3UHMJRw3m8Uiq..mDi', '김창환', 'kim2@example.com', '01099998888', '2000-05-05', '2025-01-01T11:00:00', 'USER');

INSERT INTO users (user_id, login_id, password, name, email, phone_number, birthday, created_at, role)
VALUES ('22222222-2222-2222-2222-222222222223', 'testuser3', '$2a$12$WbQzovwzqOtzDEUBjGeFcehv9OWx/ZJ0E0V3UHMJRw3m8Uiq..mDi', '장인찬', 'jang@example.com', '01099978888', '2004-07-28', '2025-01-01T11:00:00', 'USER');

INSERT INTO users (user_id, login_id, password, name, email, phone_number, birthday, created_at, role)
VALUES ('22222222-2222-2222-2222-222222222224', 'testuser4', '$2a$12$WbQzovwzqOtzDEUBjGeFcehv9OWx/ZJ0E0V3UHMJRw3m8Uiq..mDi', '최종빈', 'choi@example.com', '01099908888', '2000-01-26', '2025-01-01T11:00:00', 'USER');

INSERT INTO categories (category_name) VALUES ('탁주'), ('청주'), ('증류주'), ('약주'), ('과실주'), ('기타');

INSERT INTO Drinks (drink_id, drink_name, drink_price, drink_stock, drink_alcohol_content, drink_volume, drink_is_delete, drink_region, category_id)
VALUES
    (100, '막걸리', 8000, 100, 6.5, 750, false, '전통', 1),
    (101, '청주', 5000, 50, 13.0, 500, false, '전통', 2),
    (102, '홍주', 6000, 30, 14.0, 700, false, '전통', 3),
    (103, '한국주', 18000, 20, 15.0, 1000, false, '한국', 4),
    (104, '월량주', 10000, 40, 16.5, 750, false, '중부', 5),
    (105, '서울의밤', 12000, 25, 14.5, 600, false, '서울', 6);

INSERT INTO cart (cart_id, quantity, drink_id, user_id) VALUES (1, 1, 100, '11111111-1111-1111-1111-111111111112');
INSERT INTO cart (cart_id, quantity, drink_id, user_id) VALUES (2, 2, 102, '11111111-1111-1111-1111-111111111112');
INSERT INTO cart (cart_id, quantity, drink_id, user_id) VALUES (3, 1, 101, '22222222-2222-2222-2222-222222222222');

INSERT INTO Images(image_url) values ('https://giju-bubble.s3.ap-northeast-2.amazonaws.com/resized_12124821977126586917.png');

INSERT INTO Drink_images (image_id,drink_id,is_thumbnail) VALUES (1,100,true);
INSERT INTO Drink_images (image_id,drink_id,is_thumbnail) VALUES (1,101,true);
INSERT INTO Drink_images (image_id,drink_id,is_thumbnail) VALUES (1,102,true);
INSERT INTO Drink_images (image_id,drink_id,is_thumbnail) VALUES (1,103,true);
INSERT INTO Drink_images (image_id,drink_id,is_thumbnail) VALUES (1,104,true);
INSERT INTO Drink_images (image_id,drink_id,is_thumbnail) VALUES (1,105,true);


INSERT INTO Likes(drink_id,user_id,is_delete) VALUES (100,'22222222-2222-2222-2222-222222222222',false);

INSERT INTO Delivery_companies (delivery_company_id,delivery_company_name)
VALUES
    (100,'택배'),
    (101,'우체국 택배');

INSERT INTO Orders (
    order_id,
    total_amount,
    created_at,
    order_status,
    delivery_charge,
    is_deleted,
    order_name,
    deleted_at,
    user_id
) VALUES (
             1,
             22000,
             '2025-05-24T16:47:00',
             'PENDING',
             3000,
             false,
             'testuser1의 첫 주문',
             NULL,
             '11111111-1111-1111-1111-111111111112'
         );

INSERT INTO order_detail (
    order_detail_id,
    price,
    quantity,
    drink_name,
    order_id,
    is_canceled,
    is_refund_requested,
    is_refunded
) VALUES
      (1, 10000, 1, '막걸리', 1, false, false, false),
      (2, 12000, 1, '서울의밤', 1, false, false, false);


INSERT INTO Orders (
    order_id,
    total_amount,
    created_at,
    order_status,
    delivery_charge,
    is_deleted,
    order_name,
    deleted_at,
    user_id
) VALUES (
             2,
             22000,
             '2025-05-25T16:47:00',
             'DELIVERED',
             3000,
             false,
             'testuser1의 두번째 주문',
             NULL,
             '11111111-1111-1111-1111-111111111112'
         );

INSERT INTO order_detail (
    order_detail_id,
    price,
    quantity,
    drink_name,
    order_id,
    is_canceled,
    is_refund_requested,
    is_refunded
) VALUES
      (3, 10000, 1, '막걸리', 2, false, false, false),
      (4, 12000, 1, '서울의밤', 2, false, false, false);


INSERT INTO Deliveries(delivery_id,delivery_company_id,order_id,delivery_num)
VALUES
    (1,100,1,'12345');