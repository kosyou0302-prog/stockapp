-- =====================================
--  マスタデータの挿入
-- =====================================
INSERT INTO families (name) VALUES ('テスト用家族');
-- カテゴリマスタ
INSERT INTO categories(name) VALUES('食品');
INSERT INTO categories(name) VALUES('日用品');
INSERT INTO categories(name) VALUES('その他');

-- 単位マスタ
INSERT INTO units(name) VALUES('個');
INSERT INTO units(name) VALUES('本');
INSERT INTO units(name) VALUES('パック');
INSERT INTO units(name) VALUES('箱');

-- アイコンマスタ
INSERT INTO icons (name) VALUES
('1-icon'), ('2-icon'), ('3-icon'), ('4-icon'), ('5-icon'), ('6-icon'),
('7-icon'), ('8-icon'), ('9-icon'), ('10-icon'), ('11-icon'), ('12-icon'),
('13-icon'), ('14-icon'), ('15-icon'), ('16-icon'), ('17-icon'), ('18-icon');


-- =====================================
-- 初期商品データの挿入（全件 is_notify = true を設定）
-- =====================================

-- ① 牛乳：残りわずか(1<=2) ＆ 期限切れ(11/05) → 優先判定で「期限切れ」表示
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('牛乳', 1, 2, '2026-11-05', true, 3, 1, 2, 1, 1, false, 1);

-- ② 卵：在庫十分(10>3) ＆ 期限まぢか(3日後: 11/21、通知7日前設定) → 「期限まぢか」表示
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('卵', 10, 3, '2026-11-21', true, 7, 1, 3, 1, 1, false, 1);

-- ③ しょうゆ：在庫切れ(0) → 「在庫切れ」表示
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('しょうゆ', 0, 1, '2027-05-01', true, 7, 1, 2, 2, 2, false, 1);

-- ④ トイレットペーパー：通常在庫（アラートなし）
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('トイレットペーパー', 4, 2, NULL, true, NULL, 2, 1, 3, 1, false, 1);

-- ⑤ 防災グッズ：通常在庫（アラートなし）
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('防災グッズ', 4, 2, NULL, true, 1, 3, 1, 3, 1, false, 1);

-- ⑥ 洗濯洗剤：残りわずか(1<=2) → 「残りわずか」表示
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('洗濯洗剤', 1, 2, NULL, true, NULL, 2, 1, 1, 1, false, 1);

-- ⑦ 乾電池：その他（カテゴリ3）の「在庫 ＝ 閾値（境界値：2<=2）」確認用 → 「残りわずか」表示
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('乾電池', 2, 2, NULL, true, NULL, 3, 1, 1, 1, false, 1);

-- ⑧ 納豆：「在庫0 かつ 期限切れ」のガード確認用（在庫切れのみカウントされ、期限切れには入らない）
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('納豆', 0, 1, '2026-11-01', true, 3, 1, 3, 3, 1, false, 1);

-- ⑨ 食パン：「期限日が今日ちょうど（2026-11-18：境界値）」確認用（期限切れではなく期限まぢか・残り0日に入る）
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('食パン', 2, 1, '2026-11-18', true, 3, 1, 1, 1, 1, false, 1);

-- ⑩ 削除済みテスト商品：「論理削除済み（is_deleted = true）」の除外確認用（すべてカウント対象外）
INSERT INTO products (name, stock, threshold, expiry_date, is_notify, alert_days, category_id, unit_id, threshold_unit_id, icon_id, is_deleted, family_id) 
VALUES ('削除済みテスト商品', 0, 1, '2026-11-01', true, 3, 1, 1, 1, 1, true, 1);

-- ==========================================
-- 購入履歴テストデータ（purchase_history）
-- ※ products テーブルに ID 1〜10 が登録されている前提
-- ==========================================

-- ① 先月の購入履歴（日付順の並び替え・月またぎ確認用）
INSERT INTO purchase_history (name, quantity, record_date, family_id, product_id) 
VALUES ('牛乳', 2, '2026-10-25', 1, 1);

INSERT INTO purchase_history (name, quantity, record_date, family_id, product_id) 
VALUES ('トイレットペーパー', 1, '2026-10-28', 1, 4);

-- ② 今月初旬の購入履歴（同日に複数商品を購入したパターンの確認用）
INSERT INTO purchase_history (name, quantity, record_date, family_id, product_id) 
VALUES ('卵', 1, '2026-11-02', 1, 2);

INSERT INTO purchase_history (name, quantity, record_date, family_id, product_id) 
VALUES ('しょうゆ', 1, '2026-11-02', 1, 3);

-- ③ 同じ商品のリピート購入履歴（牛乳の2回目購入・日付重複確認用）
INSERT INTO purchase_history (name, quantity, record_date, family_id, product_id) 
VALUES ('牛乳', 1, '2026-11-02', 1, 1);

-- ④ 論理削除済み商品の購入履歴（商品が削除されても履歴名が残るかの確認用）
INSERT INTO purchase_history (name, quantity, record_date, family_id, product_id) 
VALUES ('削除済みテスト商品', 3, '2026-11-05', 1, 10);

-- ⑤ 直近（昨日・今日）の購入履歴（最新順表示の確認用）
INSERT INTO purchase_history (name, quantity, record_date, family_id, product_id) 
VALUES ('洗濯洗剤', 1, '2026-11-17', 1, 6);

INSERT INTO purchase_history (name, quantity, record_date, family_id, product_id) 
VALUES ('食パン', 2, '2026-11-18', 1, 9);