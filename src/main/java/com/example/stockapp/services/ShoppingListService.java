/*
 * 【役割】買い物リストへの追加、チェック状態の更新、一括購入処理など、
 * アプリ内で最も複雑な複数テーブル連携ロジックを担当します。
 *
 * TODO: クラスに @Service と @Transactional を付与する
 * TODO: 買い物リストへの手動・自動追加メソッドを実装する
 * TODO: 非同期通信（JS）から呼ばれるチェック状態の更新メソッドを実装する
 * TODO: 一括購入処理（対象の抽出、在庫の加算、履歴の作成、
 *       リストからの削除）を実装する
 *
 * 【今回の実装】
 * ・買い物リスト一覧取得
 * ・手動追加
 * ・在庫切れ・期限切れによる自動追加
 * ・チェック済み商品の一括購入
 *
 * 【今後の実装】
 * ・チェック状態の更新
 * ・購入予定数量の増減
 * ・チェック済み項目の一括削除
 *
 * 【項目名について】
 * Product.threshold：残りわずかを判定する購入目安
 * ShoppingListItem.threshold：買う予定の数量
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

    // ログイン機能ができるまでの仮の家族ID
    private static final Long DUMMY_FAMILY_ID = 1L;

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final ProductRepository productRepository;
    private final FamilyRepository familyRepository;
    private final PurchaseHistoryService purchaseHistoryService;

    /*
     * 【処理内容】買い物リスト一覧取得
     *
     * 現在の家族の買い物リストを取得します。
     * 商品が論理削除済みの場合は、一覧に表示しません。
     *
     * readOnly=true：
     * このメソッドではデータの更新を行いません。
     */
    @Transactional(readOnly = true)
    public List<ShoppingListItem> getShoppingList() {
        return shoppingListItemRepository
                .findByFamilyIdAndProductFamilyIdAndProductIsDeletedFalseOrderByIdAsc(
                        DUMMY_FAMILY_ID,
                        DUMMY_FAMILY_ID);
    }

    /*
     * 【処理内容】リスト追加/削除：
     * 在庫不足の商品や手動追加されたアイテムをリストに登録・解除する
     *
     * 以下に、手動追加と自動追加を実装します。
     * 購入完了時の削除はcompletePurchaseで行います。
     * 購入せずに項目を削除する処理は、今後実装します。
     */

    /*
     * ① 手動追加
     *
     * productId：追加する商品のID
     * quantity ：今回追加する数量
     *
     * ・同じ商品が未登録なら、新しい項目を作成します。
     * ・登録済みなら、既存の購入予定数量に加算します。
     * ・登録済み項目のチェック状態は変更しません。
     */
    public void addManualItem(Long productId, Integer quantity) {

        // 未入力・0・負数の数量は受け付けません。
        validateQuantity(quantity);

        // 同じ家族の追加・購入完了処理を順番に行います。
        Family family = lockCurrentFamily();

        // 同じ家族の商品で、削除済みでないことを確認します。
        Product product = getAvailableProduct(productId);

        // 同じ家族・同じ商品の登録済み項目を探します。
        ShoppingListItem existingItem = shoppingListItemRepository
                .findByFamilyIdAndProductId(
                        DUMMY_FAMILY_ID,
                        productId)
                .orElse(null);

        if (existingItem != null) {
            // 保存済みの購入予定数量も確認します。
            validateQuantity(existingItem.getThreshold());

            /*
             * 登録済みの場合は購入予定数量に加算します。
             * Math.addExactは、整数の上限を超えた場合に
             * 不正な計算結果を保存せず、例外にします。
             */
            int updatedQuantity = Math.addExact(
                    existingItem.getThreshold(),
                    quantity);

            existingItem.setThreshold(updatedQuantity);
            shoppingListItemRepository.save(existingItem);

        } else {
            // 未登録の場合は、新しい項目を作成します。
            saveNewItem(family, product, quantity);
        }
    }

    /*
     * ② 在庫連動による自動追加
     *
     * 対象：
     * ・同じ家族の商品
     * ・論理削除されていない
     * ・通知ON
     * ・在庫切れ、または期限切れ
     *
     * 購入予定数量：
     * Productの購入目安＋1
     *
     * 登録済みの場合：
     * 数量もチェック状態も変更しません。
     *
     * 確認待ちの場合：
     * 期限切れを理由にした再追加は止めます。
     * 在庫切れの場合は、確認待ちでも追加対象にします。
     *
     * ※商品をDBへ保存した後に呼び出してください。
     */
    public void addAutoItem(Product product) {

        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException(
                    "自動追加する商品が指定されていません");
        }

        Family family = lockCurrentFamily();

        /*
         * DBに保存されている商品を取得します。
         * 別の家族の商品や、削除済みの商品は対象にしません。
         */
        Product targetProduct = productRepository
                .findByIdAndFamilyIdAndIsDeletedFalse(
                        product.getId(),
                        DUMMY_FAMILY_ID)
                .orElse(null);

        if (targetProduct == null) {
            return;
        }

        // 通知OFF、または通知設定がnullの場合は追加しません。
        if (!Boolean.TRUE.equals(targetProduct.getIsNotify())) {
            return;
        }

        LocalDate today = LocalDate.now();

        // 在庫切れは、確認待ちでも自動追加の対象にします。
        boolean outOfStock = isOutOfStock(targetProduct);

        // 期限切れは、確認待ちでない場合だけ対象にします。
        boolean expiredAndNotReviewing =
                isPastExpiry(targetProduct, today)
                && !Boolean.TRUE.equals(
                        targetProduct.getNeedsReview());

        // どちらの条件にも該当しなければ終了します。
        if (!outOfStock && !expiredAndNotReviewing) {
            return;
        }

        // 同じ商品がすでに登録されているか確認します。
        boolean alreadyExists = shoppingListItemRepository
                .findByFamilyIdAndProductId(
                        DUMMY_FAMILY_ID,
                        targetProduct.getId())
                .isPresent();

        /*
         * 自動追加は、登録済みなら何もしません。
         * 判定のたびに購入予定数量が増えることを防ぎます。
         */
        if (alreadyExists) {
            return;
        }

        // 商品の購入目安は、0以上・未入力不可とします。
        Integer threshold = targetProduct.getThreshold();

        if (threshold == null || threshold < 0) {
            throw new IllegalArgumentException(
                    "商品の購入目安を0以上で設定してください");
        }

        // 購入予定数量＝商品の購入目安＋1
        int quantity = Math.addExact(threshold, 1);

        saveNewItem(family, targetProduct, quantity);
    }

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

    /*
     * 【処理内容】複数選択の一括操作：
     * 画面から送られた複数のIDリストを元に、
     * 一括削除や一括購入済みフラグ変更を行う
     *
     * 【今回の実装方針】
     * 画面からIDリストを受け取る方法ではなく、
     * DBに保存されたisChecked=trueの項目を対象にします。
     *
     * 購入完了時は、購入済みフラグを残すのではなく、
     * 購入履歴へ記録した後、買い物リストから削除します。
     *
     * 購入せずにチェック済み項目を一括削除する処理は、
     * 今後実装します。
     */

    /*
     * ③ チェック済み商品の一括購入
     *
     * 処理の流れ：
     * 1. 現在の家族のチェック済み項目を取得する
     * 2. 購入予定数量を商品の在庫数に加算する
     * 3. 購入前に期限切れなら、確認用フラグをtrueにする
     * 4. 購入履歴を記録する
     * 5. 購入済み項目を買い物リストから削除する
     *
     * 戻り値：
     * 購入完了にした項目数
     * ※商品の合計個数ではありません。
     *
     * 途中で例外が発生した場合は、
     * 同じトランザクション内の変更を取り消します。
     */
    public int completePurchase() {

        /*
         * 同じ家族の購入完了処理が同時に進まないようにします。
         * 後から実行された処理は、先の処理で削除された項目を
         * 再度購入対象にしないようにします。
         */
        lockCurrentFamily();

        // 同じ家族の、削除されていない商品のチェック済み項目を取得します。
        List<ShoppingListItem> checkedItems =
                shoppingListItemRepository
                        .findByFamilyIdAndProductFamilyIdAndIsCheckedTrueAndProductIsDeletedFalseOrderByIdAsc(
                                DUMMY_FAMILY_ID,
                                DUMMY_FAMILY_ID);

        if (checkedItems.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();

        for (ShoppingListItem item : checkedItems) {
            Product product = item.getProduct();

            // ShoppingListItem.thresholdは購入予定数量です。
            Integer quantity = item.getThreshold();
            validateQuantity(quantity);

            Integer currentStock = product.getStock();

            if (currentStock == null || currentStock < 0) {
                throw new IllegalArgumentException(
                        "商品の在庫数を確認してください");
            }

            // 購入前に期限切れだったかを確認します。
            boolean expiredBeforePurchase =
                    isPastExpiry(product, today);

            // 購入予定数量を、現在の在庫数に加算します。
            int updatedStock = Math.addExact(
                    currentStock,
                    quantity);

            product.setStock(updatedStock);

            /*
             * 期限切れの状態で購入した場合は確認待ちにします。
             * 処分した数量や新しい使用期限は、自動では変更しません。
             *
             * 期限切れでない場合も、既存の確認待ちフラグを
             * この処理から勝手に解除しません。
             */
            if (expiredBeforePurchase) {
                product.setNeedsReview(true);
            }

            productRepository.save(product);

            /*
             * 購入履歴の保存を担当Serviceへ依頼します。
             *
             * ※recordPurchaseは連携用の仮のメソッド名です。
             * PurchaseHistoryService担当者と名前・引数を合わせてください。
             * 履歴保存は購入完了処理と同じトランザクションで行います。
             */
            purchaseHistoryService.recordPurchase(
                    product,
                    quantity);
        }

        // 購入完了した項目だけを買い物リストから削除します。
        shoppingListItemRepository.deleteAll(checkedItems);

        return checkedItems.size();
    }

    /*
     * 【共通処理】新しい買い物リスト項目を保存する
     *
     * 新規追加時のチェック状態はfalseにします。
     */
    private void saveNewItem(
            Family family,
            Product product,
            Integer quantity) {

        ShoppingListItem item = new ShoppingListItem();

        item.setFamily(family);
        item.setProduct(product);
        item.setThreshold(quantity);
        item.setIsChecked(false);

        shoppingListItemRepository.save(item);
    }

    /*
     * 【共通処理】購入予定数量の確認
     *
     * 数量は1以上とします。
     */
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException(
                    "購入予定数量は1以上で入力してください");
        }
    }

    /*
     * 【共通処理】追加可能な商品を取得する
     *
     * 同じ家族の商品で、削除済みでないものだけ取得します。
     */
    private Product getAvailableProduct(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException(
                    "商品を選択してください");
        }

        return productRepository
                .findByIdAndFamilyIdAndIsDeletedFalse(
                        productId,
                        DUMMY_FAMILY_ID)
                .orElseThrow(() -> new IllegalArgumentException(
                        "追加できる商品が見つかりません"));
    }

    /*
     * 【共通処理】家族データの取得・ロック
     *
     * 同じ家族の追加・購入完了処理を順番に行うため、
     * 家族データのロックを取得します。
     *
     * ロックはトランザクション終了時に解放されます。
     * 今後追加する数量変更・チェック更新・削除も、
     * 同じ順序でロックを取得する方針にそろえます。
     */
    private Family lockCurrentFamily() {
        return familyRepository
                .findByIdForUpdate(DUMMY_FAMILY_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "家族データが登録されていません"));
    }

    /*
     * 【判定】在庫切れ
     *
     * 在庫数が0なら在庫切れです。
     * Entity側の判定メソッドが確定したら、
     * そのメソッドの呼び出しに置き換えます。
     */
    private boolean isOutOfStock(Product product) {
        return product.getStock() != null
                && product.getStock() == 0;
    }

    /*
     * 【判定】期限切れ
     *
     * 使用期限が今日より前なら期限切れです。
     * 使用期限が今日の場合は、期限切れに含めません。
     *
     * Entity側の判定メソッドが確定したら、
     * そのメソッドの呼び出しに置き換えます。
     */
    private boolean isPastExpiry(Product product, LocalDate today) {
        return product.getExpiryDate() != null
                && product.getExpiryDate().isBefore(today);
    }
}