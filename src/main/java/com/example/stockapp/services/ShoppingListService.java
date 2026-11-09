/*
 * 【役割】買い物リストへの追加、チェック状態の更新、一括購入処理など、
 * アプリ内で最も複雑な複数テーブル連携ロジックを担当します。
 *
 * ※ログイン機能ができるまでは、ダミーの家族ID＝1を使用します。
 */
package com.example.stockapp.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockapp.entities.Family;
import com.example.stockapp.entities.Product;
import com.example.stockapp.entities.ShoppingListItem;
import com.example.stockapp.repositories.FamilyRepository;
import com.example.stockapp.repositories.ProductRepository;
import com.example.stockapp.repositories.ShoppingListItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ShoppingListService {
    // ==================================================
    // 定数・使用するRepositoryとService
    // ==================================================
    // ログイン機能ができるまでの仮の家族ID
    private static final Long DUMMY_FAMILY_ID = 1L;
    // 買い物リストの取得・保存・削除
    private final ShoppingListItemRepository shoppingListItemRepository;
    // 商品の取得・在庫数などの保存
    private final ProductRepository productRepository;
    // 家族データの取得・ロック
    private final FamilyRepository familyRepository;
    // 購入履歴の記録
    private final PurchaseHistoryService purchaseHistoryService;

    // ==================================================
    // ➀　買い物リスト一覧取得
    // ==================================================
    /*
     * 【処理内容】買い物リスト一覧取得
     * 現在の家族の買い物リストを取得します。
     * 商品が論理削除済みの場合は、一覧に表示しません。
     * readOnly=true：
     * このメソッドではデータの更新を行いません。
     */
    @Transactional(readOnly = true)
    public List<ShoppingListItem> getShoppingList() {
        return shoppingListItemRepository.findByFamily_IdAndProduct_Family_IdAndProduct_IsDeletedFalseOrderByIdAsc(
            DUMMY_FAMILY_ID, DUMMY_FAMILY_ID);
    }

    // ==================================================
    // ➁ 手動追加
    // ==================================================
    /**
     * 商品を手動で買い物リストに追加します。
     * 未登録なら新規作成、登録済みなら購入予定数量を加算します。
     */
    public void addManualItem(Long productId, Integer quantity) {
        // 1. 入力値検証（数量は1以上）
        validateQuantity(quantity);

        // 2. ログイン機能ができるまでの暫定対応：ID=1のダミー家族をセット)
        // 排他ロック処理（将来の拡張用に保持）Family family = lockCurrentFamily();
        Family family = familyRepository.findById(DUMMY_FAMILY_ID)
        .orElseThrow(() -> new IllegalStateException("ダミー家族が見つかりません"));
        Product product = getAvailableProduct(productId);

        // 3. 買い物リストの既存登録を確認
        ShoppingListItem existingItem = shoppingListItemRepository
                .findByFamily_IdAndProduct_Id(DUMMY_FAMILY_ID, productId)
                .orElse(null);

        // 4. 登録状況に応じた保存処理
        if (existingItem != null) {
            // 登録済み：既存数量に加算（オーバーフロー検知）
            validateQuantity(existingItem.getThreshold());
            int updatedQuantity = Math.addExact(existingItem.getThreshold(), quantity);

            // チェック状態は変更せず、数量だけ更新
            existingItem.setThreshold(updatedQuantity);
            shoppingListItemRepository.save(existingItem);
        } else {
            // 未登録：チェックなし（false）で新規登録
            saveNewItem(family, product, quantity);
        }
    }

    // ==================================================
    // ➂ 自動追加
    // ==================================================
    /**
     * 在庫数または賞味期限の状態に応じて、商品を買い物リストへ自動追加。
     * ※商品をDBへ保存した後に呼び出し。
     */
    public void addAutoItem(Product product) {
        // 1. 引数検証
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("自動追加する商品が指定されていません");
        }

        // 2. 家族情報取得と商品の最新状態・権限確認（他家族・削除済みを除外）
        // 【将来拡張用：悲観的ロック】Family family = lockCurrentFamily();
        // ログイン機能ができるまでの暫定対応：ID=1のダミー家族をセット)
        Family family = familyRepository.findById(DUMMY_FAMILY_ID)
        .orElseThrow(() -> new IllegalStateException("ダミー家族が見つかりません"));

        Product targetProduct = productRepository
            .findByIdAndFamily_IdAndIsDeletedFalse(product.getId(), DUMMY_FAMILY_ID)
            .orElse(null);
        // 別の家族の商品・削除済み商品・存在しない商品は対象外です。
        if (targetProduct == null) {
            return;
        }

        // 3. 通知設定の確認（通知OFFならスキップ）
        if (!Boolean.TRUE.equals(targetProduct.getIsNotify())) {
            return;
        }

        // 4. アラート判定（在庫切れ、または期限切れ）
        LocalDate today = LocalDate.now();
        boolean outOfStock = isOutOfStock(targetProduct);
        boolean expired = isPastExpiry(targetProduct, today);

        /*
         * 【将来拡張用：要確認フラグ連携】
         * boolean needsReview = Boolean.TRUE.equals(targetProduct.getNeedsReview());
         * boolean expiredAndNotReviewing = expired && !needsReview;
         * if (!outOfStock && !expiredAndNotReviewing) { return; }
         */
        // 在庫切れでも期限切れでもなければ終了
        if(!outOfStock && !expired) {
            return;
        }

        // 5. 二重登録防止（すでにリストにある場合はスキップ）
        boolean alreadyExists = shoppingListItemRepository
                .findByFamily_IdAndProduct_Id(DUMMY_FAMILY_ID, targetProduct.getId())
                .isPresent();

        if (alreadyExists) {
            return;
        }

        // 6. 購入予定数量の算出（購入目安 + 1）
        Integer threshold = targetProduct.getThreshold();
        if (threshold == null || threshold < 0) {
            throw new IllegalArgumentException("商品の購入目安を0以上で設定してください");
        }
        int quantity = Math.addExact(threshold, 1);

        // 7. 買い物リストへ新規保存
        saveNewItem(family, targetProduct, quantity);
    }

    // ==================================================
    // 数量変更・チェック状態の更新（今後実装）
    // ==================================================

    /*
     * 【処理内容】数量の増減：
     * JSからの非同期リクエストを受け取り、
     * 指定アイテムの数量を「＋1」または「-1」して更新する
     *
     * ※今後実装します。
     * 実際のリクエストはControllerで受け取り、
     * ControllerからServiceのメソッドを呼び出します。
     */
    /*
     * 【処理内容】チェック状態の更新
     *
     * ※今後実装します。
     * ShoppingListItemのisCheckedを更新する処理です。
     */

    // ==================================================
    // ➃ チェック済み商品の一括購入
    // ==================================================
    /**
     * チェック済みの商品を一括で購入完了とし、在庫更新・履歴保存・リスト削除を行います。
     *
     * @return 購入完了にしたアイテム数
     */
    public int completePurchase() {
        // 1. 排他制御
        // 【将来拡張用：悲観的ロック】lockCurrentFamily();

        // 2. チェック済み項目の取得（自家族・未削除商品のみ）
        List<ShoppingListItem> checkedItems = shoppingListItemRepository.findByFamily_IdAndIsCheckedTrue(DUMMY_FAMILY_ID);
        // 購入対象がなければ、購入完了件数0を返します。
        if (checkedItems.isEmpty()) {
            return 0;
        }

        // 3. 各商品の在庫更新と履歴記録
        for (ShoppingListItem item : checkedItems) {
            Product product = item.getProduct();

            // 商品が存在しない、または論理削除済みの場合は在庫更新をスキップ
            if(product == null || Boolean.TRUE.equals(product.getIsDeleted())) {
                continue;
            }

            Integer quantity = item.getThreshold();
            validateQuantity(quantity);

            Integer currentStock = product.getStock();
            if (currentStock == null || currentStock < 0) {
                throw new IllegalArgumentException("商品の在庫数を確認してください");
            }

            // 在庫数に購入数量を加算してセット（オーバーフロー検知）
            int updatedStock = Math.addExact(currentStock, quantity);
            product.setStock(updatedStock);

            /*
            * 【将来拡張用：要確認フラグ連携】
            * LocalDate today = LocalDate.now();
            * boolean expiredBeforePurchase = isPastExpiry(product, today);
            * if (expiredBeforePurchase) {
            *     product.setNeedsReview(true);
            * }
            */
            // 商品在庫の更新保存
            productRepository.save(product);

            purchaseHistoryService.recordPurchase(product, quantity);
        }

        // リストからの削除と件数返却
        shoppingListItemRepository.deleteAll(checkedItems);
        return checkedItems.size();
    }

    // ==================================================
    // 共通処理
    // ==================================================

    //  新しい買い物リスト項目を作成・保存します（初期チェック状態は false）。
    private void saveNewItem(Family family, Product product, Integer quantity) {
        ShoppingListItem item = new ShoppingListItem();
        // 2. 家族・商品・購入予定数量・チェック状態を設定します。
        item.setFamily(family);
        item.setProduct(product);
        item.setThreshold(quantity);
        item.setIsChecked(false);

        // 3. 買い物リストに保存します。
        shoppingListItemRepository.save(item);
    }

    // 購入予定数量の入力値検証（1以上必須）
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("購入予定数量は1以上で入力してください");
        }
    }

    // 追加可能な有効商品を取得します（自家族かつ論理削除されていない商品）
    private Product getAvailableProduct(Long productId) {
        // 1. 商品IDが指定されているか確認します。
        if (productId == null) {
            throw new IllegalArgumentException("商品を選択してください");
        }
        // 2. 条件に一致する商品を取得します。
        // 見つからない場合は、例外にして追加処理を止めます。
        return productRepository
            .findByIdAndFamily_IdAndIsDeletedFalse(productId, DUMMY_FAMILY_ID)
            .orElseThrow(() -> new IllegalArgumentException("追加できる商品が見つかりません"));
    }

    // 家族データの排他ロック（FOR UPDATE）を取得します。
    // 同一家族に対する追加・購入処理を直列化して整合性を保ちます。
    private Family lockCurrentFamily() {
        return familyRepository.findById(DUMMY_FAMILY_ID)
                .orElseThrow(() -> new IllegalStateException("家族データが登録されていません"));
    }

    // ==================================================
    // 在庫・使用期限の判定
    // ==================================================

    // 商品が在庫切れかどうかを判定します（在庫数が0以下）
    private boolean isOutOfStock(Product product) {
        return product != null
            && product.getStock() != null
            && product.getStock() <= 0;
    }

    // 商品が使用期限切れかどうかを判定します（今日より前なら期限切れ）。
    // ※今日当日は期限切れに含めません。
    private boolean isPastExpiry(Product product, LocalDate today) {
        if (product == null || product.getExpiryDate() == null || today == null) {
            return false;
        }
        return product.getExpiryDate().isBefore(today);
    }
}