-- =====================================
--  マスタデータの挿入
-- =====================================
-- カテゴリマスタ(name)
INSERT INTO categories(name) VALUES('食品');
INSERT INTO categories(name) VALUES('日用品');
INSERT INTO categories(name) VALUES('その他');

-- 単位マスタ
INSERT INTO units(name) VALUES('個');
INSERT INTO units(name) VALUES('本');
INSERT INTO units(name) VALUES('パック');
INSERT INTO units(name) VALUES('箱');

-- アイコンマスタ
INSERT INTO icons (name) VALUES ('1-icon');
INSERT INTO icons (name) VALUES ('2-icon');
INSERT INTO icons (name) VALUES ('3-icon');
INSERT INTO icons (name) VALUES ('4-icon');
INSERT INTO icons (name) VALUES ('5-icon');


-- =====================================
-- 初期商品データの挿入
-- =====================================

INSERT INTO products (name, stock, threshold, expiry_date, alert_days, category_id, unit_id, icon_id, is_deleted) 
VALUES ('牛乳', 1, 2, '2026-11-05', 3, 1, 2, 1, false);

INSERT INTO products (name, stock, threshold, expiry_date, alert_days, category_id, unit_id, icon_id, is_deleted) 
VALUES ('卵', 10, 3, '2026-11-15', 5, 1, 3, 1, false);

INSERT INTO products (name, stock, threshold, expiry_date, alert_days, category_id, unit_id, icon_id, is_deleted) 
VALUES ('しょうゆ', 0, 1, '2027-05-01', 7, 2, 2, 2, false);

INSERT INTO products (name, stock, threshold, expiry_date, alert_days, category_id, unit_id, icon_id, is_deleted) 
VALUES ('トイレットペーパー', 4, 2, NULL, 0, 3, 1, 3, false);