INSERT INTO payment (payment_key, amount, payment_method, payment_status, approved_at, transaction_key, approve_no, receipt_url, cash_receipt_url, order_id)
VALUES
-- Order 1 (DELIVERED)
('payment_key_001_a1b2c3d4e5f6', 15500, '카드', 'DONE', '2024-12-10T14:35:00+09:00', 'txn_001_a1b2c3d4', 'APV_001_123456', 'https://receipt.toss.im/payment_001', NULL, 1),

-- Order 2 (DELIVERED)
('payment_key_002_b2c3d4e5f6g7', 14400, '간편결제', 'DONE', '2024-12-15T19:20:00+09:00', 'txn_002_b2c3d4e5', 'APV_002_234567', 'https://receipt.toss.im/payment_002', NULL, 2),

-- Order 3 (DELIVERED)
('payment_key_003_c3d4e5f6g7h8', 41700, '카드', 'DONE', '2024-12-25T16:25:00+09:00', 'txn_003_c3d4e5f6', 'APV_003_345678', 'https://receipt.toss.im/payment_003', NULL, 3),

-- Order 4 (DELIVERED)
('payment_key_004_d4e5f6g7h8i9', 25000, '계좌이체', 'DONE', '2025-01-05T11:50:00+09:00', 'txn_004_d4e5f6g7', 'APV_004_456789', NULL, 'https://cashreceipt.toss.im/payment_004', 4),

-- Order 5 (DELIVERED)
('payment_key_005_e5f6g7h8i9j0', 19000, '간편결제', 'DONE', '2025-01-12T15:25:00+09:00', 'txn_005_e5f6g7h8', 'APV_005_567890', 'https://receipt.toss.im/payment_005', NULL, 5),

-- Order 6 (DELIVERED)
('payment_key_006_f6g7h8i9j0k1', 34000, '카드', 'DONE', '2025-01-18T20:15:00+09:00', 'txn_006_f6g7h8i9', 'APV_006_678901', 'https://receipt.toss.im/payment_006', NULL, 6),

-- Order 7 (DELIVERED)
('payment_key_007_g7h8i9j0k1l2', 20000, '간편결제', 'DONE', '2025-01-22T13:35:00+09:00', 'txn_007_g7h8i9j0', 'APV_007_789012', 'https://receipt.toss.im/payment_007', NULL, 7),

-- Order 8 (DELIVERED)
('payment_key_008_h8i9j0k1l2m3', 14000, '가상계좌', 'DONE', '2025-01-26T17:50:00+09:00', 'txn_008_h8i9j0k1', 'APV_008_890123', NULL, 'https://cashreceipt.toss.im/payment_008', 8),

-- Order 11 (DELIVERED)
('payment_key_011_k1l2m3n4o5p6', 18100, '간편결제', 'DONE', '2024-11-20T10:25:00+09:00', 'txn_011_k1l2m3n4', 'APV_011_123789', 'https://receipt.toss.im/payment_011', NULL, 11),

-- Order 12 (DELIVERED)
('payment_key_012_l2m3n4o5p6q7', 25800, '카드', 'DONE', '2024-12-05T14:55:00+09:00', 'txn_012_l2m3n4o5', 'APV_012_234890', 'https://receipt.toss.im/payment_012', NULL, 12),

-- Order 13 (DELIVERED)
('payment_key_013_m3n4o5p6q7r8', 29000, '간편결제', 'DONE', '2024-12-20T18:45:00+09:00', 'txn_013_m3n4o5p6', 'APV_013_345901', 'https://receipt.toss.im/payment_013', NULL, 13),

-- Order 14 (DELIVERED)
('payment_key_014_n4o5p6q7r8s9', 35000, '카드', 'DONE', '2025-01-08T13:30:00+09:00', 'txn_014_n4o5p6q7', 'APV_014_456012', 'https://receipt.toss.im/payment_014', NULL, 14),

-- Order 15 (DELIVERED)
('payment_key_015_o5p6q7r8s9t0', 19600, '계좌이체', 'DONE', '2025-01-14T16:15:00+09:00', 'txn_015_o5p6q7r8', 'APV_015_567123', NULL, 'https://cashreceipt.toss.im/payment_015', 15),

-- Order 16 (DELIVERED)
('payment_key_016_p6q7r8s9t0u1', 30000, '간편결제', 'DONE', '2025-01-20T19:35:00+09:00', 'txn_016_p6q7r8s9', 'APV_016_678234', 'https://receipt.toss.im/payment_016', NULL, 16),

-- Order 19 (DELIVERED)
('payment_key_019_s9t0u1v2w3x4', 28500, '카드', 'DONE', '2024-10-15T12:35:00+09:00', 'txn_019_s9t0u1v2', 'APV_019_901567', 'https://receipt.toss.im/payment_019', NULL, 19),

-- Order 20 (DELIVERED)
('payment_key_020_t0u1v2w3x4y5', 21000, '간편결제', 'DONE', '2024-11-10T17:25:00+09:00', 'txn_020_t0u1v2w3', 'APV_020_012678', 'https://receipt.toss.im/payment_020', NULL, 20),

-- Order 21 (DELIVERED)
('payment_key_021_u1v2w3x4y5z6', 25000, '카드', 'DONE', '2024-12-01T14:50:00+09:00', 'txn_021_u1v2w3x4', 'APV_021_123789', 'https://receipt.toss.im/payment_021', NULL, 21),

-- Order 22 (DELIVERED)
('payment_key_022_v2w3x4y5z6a7', 23000, '가상계좌', 'DONE', '2024-12-28T10:20:00+09:00', 'txn_022_v2w3x4y5', 'APV_022_234890', NULL, 'https://cashreceipt.toss.im/payment_022', 22),

-- Order 23 (DELIVERED)
('payment_key_023_w3x4y5z6a7b8', 17000, '간편결제', 'DONE', '2025-01-15T13:55:00+09:00', 'txn_023_w3x4y5z6', 'APV_023_345901', 'https://receipt.toss.im/payment_023', NULL, 23),

-- Order 25 (DELIVERED)
('payment_key_025_y5z6a7b8c9d0', 11500, '카드', 'DONE', '2024-09-20T11:45:00+09:00', 'txn_025_y5z6a7b8', 'APV_025_567123', 'https://receipt.toss.im/payment_025', NULL, 25),

-- Order 26 (DELIVERED)
('payment_key_026_z6a7b8c9d0e1', 38000, '간편결제', 'DONE', '2024-10-25T15:35:00+09:00', 'txn_026_z6a7b8c9', 'APV_026_678234', 'https://receipt.toss.im/payment_026', NULL, 26),

-- Order 27 (DELIVERED)
('payment_key_027_a7b8c9d0e1f2', 48000, '카드', 'DONE', '2024-11-15T18:25:00+09:00', 'txn_027_a7b8c9d0', 'APV_027_789345', 'https://receipt.toss.im/payment_027', NULL, 27),

-- Order 28 (DELIVERED)
('payment_key_028_b8c9d0e1f2g3', 28000, '간편결제', 'DONE', '2024-12-08T12:15:00+09:00', 'txn_028_b8c9d0e1', 'APV_028_890456', 'https://receipt.toss.im/payment_028', NULL, 28),

-- Order 29 (DELIVERED)
('payment_key_029_c9d0e1f2g3h4', 31000, '계좌이체', 'DONE', '2024-12-30T14:55:00+09:00', 'txn_029_c9d0e1f2', 'APV_029_901567', NULL, 'https://cashreceipt.toss.im/payment_029', 29),

-- Order 30 (DELIVERED)
('payment_key_030_d0e1f2g3h4i5', 50000, '카드', 'DONE', '2025-01-10T16:45:00+09:00', 'txn_030_d0e1f2g3', 'APV_030_012678', 'https://receipt.toss.im/payment_030', NULL, 30),

-- Order 32 (DELIVERED)
('payment_key_032_f2g3h4i5j6k7', 7500, '카드', 'DONE', '2024-08-15T10:25:00+09:00', 'txn_032_f2g3h4i5', 'APV_032_234890', 'https://receipt.toss.im/payment_032', NULL, 32),

-- Order 33 (DELIVERED)
('payment_key_033_g3h4i5j6k7l8', 9200, '카드', 'DONE', '2024-09-10T14:35:00+09:00', 'txn_033_g3h4i5j6', 'APV_033_345901', 'https://receipt.toss.im/payment_033', NULL, 33),

-- Order 34 (DELIVERED)
('payment_key_034_h4i5j6k7l8m9', 13000, '가상계좌', 'DONE', '2024-10-05T16:50:00+09:00', 'txn_034_h4i5j6k7', 'APV_034_456012', NULL, 'https://cashreceipt.toss.im/payment_034', 34),

-- Order 35 (DELIVERED)
('payment_key_035_i5j6k7l8m9n0', 15880, '카드', 'DONE', '2024-11-02T12:20:00+09:00', 'txn_035_i5j6k7l8', 'APV_035_567123', 'https://receipt.toss.im/payment_035', NULL, 35),

-- Order 36 (DELIVERED)
('payment_key_036_j6k7l8m9n0o1', 19200, '카드', 'DONE', '2024-11-25T17:35:00+09:00', 'txn_036_j6k7l8m9', 'APV_036_678234', 'https://receipt.toss.im/payment_036', NULL, 36),

-- Order 37 (DELIVERED)
('payment_key_037_k7l8m9n0o1p2', 22000, '카드', 'DONE', '2024-12-12T13:25:00+09:00', 'txn_037_k7l8m9n0', 'APV_037_789345', 'https://receipt.toss.im/payment_037', NULL, 37),

-- Order 38 (DELIVERED)
('payment_key_038_l8m9n0o1p2q3', 14000, '계좌이체', 'DONE', '2024-12-18T15:45:00+09:00', 'txn_038_l8m9n0o1', 'APV_038_890456', NULL, 'https://cashreceipt.toss.im/payment_038', 38),

-- Order 39 (DELIVERED)
('payment_key_039_m9n0o1p2q3r4', 26000, '카드', 'DONE', '2025-01-03T11:55:00+09:00', 'txn_039_m9n0o1p2', 'APV_039_901567', 'https://receipt.toss.im/payment_039', NULL, 39),

-- Order 40 (DELIVERED)
('payment_key_040_n0o1p2q3r4s5', 8580, '카드', 'DONE', '2025-01-09T14:15:00+09:00', 'txn_040_n0o1p2q3', 'APV_040_012678', 'https://receipt.toss.im/payment_040', NULL, 40),

-- Order 41 (DELIVERED)
('payment_key_041_o1p2q3r4s5t6', 16000, '카드', 'DONE', '2025-01-16T16:30:00+09:00', 'txn_041_o1p2q3r4', 'APV_041_123789', 'https://receipt.toss.im/payment_041', NULL, 41),

-- Order 42 (DELIVERED)
('payment_key_042_p2q3r4s5t6u7', 19000, '가상계좌', 'DONE', '2025-01-20T18:40:00+09:00', 'txn_042_p2q3r4s5', 'APV_042_234890', NULL, 'https://cashreceipt.toss.im/payment_042', 42),

-- Order 43 (DELIVERED)
('payment_key_043_q3r4s5t6u7v8', 21000, '카드', 'DONE', '2024-07-20T10:20:00+09:00', 'txn_043_q3r4s5t6', 'APV_043_345901', 'https://receipt.toss.im/payment_043', NULL, 43),

-- Order 44 (DELIVERED)
('payment_key_044_r4s5t6u7v8w9', 15000, '카드', 'DONE', '2024-08-28T13:45:00+09:00', 'txn_044_r4s5t6u7', 'APV_044_456012', 'https://receipt.toss.im/payment_044', NULL, 44),

-- Order 45 (DELIVERED)
('payment_key_045_s5t6u7v8w9x0', 18000, '카드', 'DONE', '2024-09-15T15:25:00+09:00', 'txn_045_s5t6u7v8', 'APV_045_567123', 'https://receipt.toss.im/payment_045', NULL, 45),

-- Order 46 (DELIVERED)
('payment_key_046_t6u7v8w9x0y1', 12880, '계좌이체', 'DONE', '2024-10-12T17:55:00+09:00', 'txn_046_t6u7v8w9', 'APV_046_678234', NULL, 'https://cashreceipt.toss.im/payment_046', 46),

-- Order 47 (DELIVERED)
('payment_key_047_u7v8w9x0y1z2', 20000, '카드', 'DONE', '2024-11-08T12:35:00+09:00', 'txn_047_u7v8w9x0', 'APV_047_789345', 'https://receipt.toss.im/payment_047', NULL, 47),

-- Order 48 (DELIVERED)
('payment_key_048_v8w9x0y1z2a3', 14000, '카드', 'DONE', '2024-12-03T14:50:00+09:00', 'txn_048_v8w9x0y1', 'APV_048_890456', 'https://receipt.toss.im/payment_048', NULL, 48),

-- Order 49 (DELIVERED)
('payment_key_049_w9x0y1z2a3b4', 23000, '가상계좌', 'DONE', '2024-12-22T16:15:00+09:00', 'txn_049_w9x0y1z2', 'APV_049_901567', NULL, 'https://cashreceipt.toss.im/payment_049', 49),

-- Order 50 (DELIVERED)
('payment_key_050_x0y1z2a3b4c5', 31000, '카드', 'DONE', '2025-01-02T11:30:00+09:00', 'txn_050_x0y1z2a3', 'APV_050_012678', 'https://receipt.toss.im/payment_050', NULL, 50),

-- Order 51 (DELIVERED)
('payment_key_051_y1z2a3b4c5d6', 17000, '카드', 'DONE', '2025-01-11T14:00:00+09:00', 'txn_051_y1z2a3b4', 'APV_051_123789', 'https://receipt.toss.im/payment_051', NULL, 51),

-- Order 52 (DELIVERED)
('payment_key_052_z2a3b4c5d6e7', 25000, '카드', 'DONE', '2025-01-19T15:45:00+09:00', 'txn_052_z2a3b4c5', 'APV_052_234890', 'https://receipt.toss.im/payment_052', NULL, 52),

-- Order 53 (DELIVERED)
('payment_key_053_a3b4c5d6e7f8', 19000, '계좌이체', 'DONE', '2025-01-25T17:20:00+09:00', 'txn_053_a3b4c5d6', 'APV_053_345901', NULL, 'https://cashreceipt.toss.im/payment_053', 53);

-- Auto-increment 값 재설정
ALTER TABLE payment ALTER COLUMN payment_id RESTART WITH 51;