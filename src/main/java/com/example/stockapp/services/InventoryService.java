/*
 * 【役割】在庫一覧画面、アラート画面、ホーム画面の表示に必要なデータを取得・集計する「参照専用」のロジックを担当します。
　　　　　 商品情報の参照処理を担当するサービスクラス
　　　　　 登録・更新・削除は行わず、商品データの取得や集計のみを行う
 */
package com.example.stockapp.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockapp.entities.Product;
import com.example.stockapp.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final ProductService productService;

    // ログイン機能ができるまでの暫定対応：ID=1のダミー家族をセット)
    private static final Long FAMILY_ID = 1L;
    /*Family dummyFamily = familyRepository.findById(1L).orElseThrow();
    product.setFamily(dummyFamily); */

    // 【処理内容】全在庫一覧取得：論理削除されていない商品を取得する
    public List<Product> getAllInventory() {
        List<Product> products =
            productRepository.findByFamily_IdAndIsDeletedFalseOrderByNameAsc(FAMILY_ID);
        return products;
    }

    // 【処理内容】商品名によるあいまい検索：商品名の部分一致検索を行う
    // 既存の呼び出し用として残す（検索結果はID昇順）
    public List<Product> searchInventory(String keyword) {
        // キーワード未入力の場合は全件取得
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllInventory();
        }
        List<Product> products =productRepository
            .findByFamily_IdAndNameContainingAndIsDeletedFalseOrderByIdAsc(FAMILY_ID, keyword);
            return products;
    }

    // 【処理内容】商品名とカテゴリーを組み合わせたあいまい検索
    // 在庫一覧の検索フォームから受け取った条件で取得する（結果は商品名昇順）
    public List<Product> searchInventory(String keyword, Long categoryId) {
        // 検索文字がnullの場合は空文字とし、前後の余分な空白を取り除く
        String searchKeyword = keyword == null ? "" : keyword.trim();

        // キーワード未入力の場合は、選択中のカテゴリーの商品を取得する
        // カテゴリーも未指定の場合は全件取得する
        if (searchKeyword.isEmpty()) {
            return getInventoryByCategory(categoryId);
        }

        // 「すべて」の場合は、全カテゴリーから商品名で検索する
        if (categoryId == null) {
            return productRepository
                    .findByNameContainingAndFamily_IdAndIsDeletedFalseOrderByNameAsc(
                            searchKeyword, FAMILY_ID);
        }

        // カテゴリー選択中の場合は、そのカテゴリー内で商品名を検索する
        return productRepository
                .findByNameContainingAndCategory_IdAndFamily_IdAndIsDeletedFalseOrderByNameAsc(
                        searchKeyword, categoryId, FAMILY_ID);
    }

    //カテゴリ別一覧取得
    public List<Product> getInventoryByCategory(Long categoryId) {
        if (categoryId == null) {
            return getAllInventory();
        }
        return productRepository
            .findByCategory_IdAndFamily_IdAndIsDeletedFalseOrderByNameAsc(
                        categoryId,FAMILY_ID);
    }

    // 【処理内容】アラート対象抽出：
    // 「在庫切れ・残りわずか」または「期限切れ・期限間近」の商品を取得する
    public List<Product> getAlertInventory() {
        List<Product> allProducts = getAllInventory();
        List<Product> alertProducts =allProducts.stream()
                    .filter(product ->
                        productService.isOutOfStock(product)
                        || productService.isLowStock(product)
                        || productService.isPastExpiry(product)
                        || productService.isNearExpiry(product))
                    .toList();
        return alertProducts;
    }

    // 【処理内容】アラート対象商品を、商品名とアラート種別で絞り込む
    // アラート画面のControllerから呼び出す（結果は商品名昇順）
    public List<Product> searchAlertInventory(String keyword, String type) {
        // 現在の家族の未削除商品を、商品名で部分一致検索する
        // カテゴリーは指定しない。キーワード未入力の場合は全件取得する。
        List<Product> products = searchInventory(keyword, null);
        String alertType = type == null ? "" : type;

        return products.stream()
                // 通知ONの商品だけをアラートの表示対象にする
                // 通知OFF・未設定（null）の商品は除外する
                .filter(product -> Boolean.TRUE.equals(product.getIsNotify()))

                // 選択したアラート種別に該当する商品を抽出する
                .filter(product -> switch (alertType) {
                    // 在庫切れ・残りわずか
                    case "stock" -> productService.isOutOfStock(product)
                            || productService.isLowStock(product);

                    // 期限切れ・期限間近
                    case "expiry" -> productService.isPastExpiry(product)
                            || productService.isNearExpiry(product);

                    // ホーム画面から個別のアラートを選んだ場合
                    case "out" -> productService.isOutOfStock(product);
                    case "low" -> productService.isLowStock(product);
                    case "expired" -> productService.isPastExpiry(product);
                    case "soon" -> productService.isNearExpiry(product);

                    // 「すべて」：4種類のいずれかに該当する商品
                    default -> productService.isOutOfStock(product)
                            || productService.isLowStock(product)
                            || productService.isPastExpiry(product)
                            || productService.isNearExpiry(product);
                })
                .toList();
    }

    // 【処理内容】ホーム画面用集計：
    // 在庫切れ件数・残りわずか件数・期限切れ件数・期限間近件数を集計する
    public Map<String, Long> getHomeSummary() {
        List<Product> products = getAllInventory();

        long outOfStockCount = 0L;
        long lowStockCount = 0L;
        long expiredCount = 0L;
        long nearExpiryCount = 0L;

        for(Product product : products) {
            if (productService.isOutOfStock(product)) {
                outOfStockCount++;
            }else if(productService.isLowStock(product)) {
                lowStockCount++;
            }
            if (productService.isPastExpiry(product)) {
                expiredCount++;
            }else if(productService.isNearExpiry(product)) {
                nearExpiryCount++;
            }
        }

        // 4回全件ループしてしまうので↑に変更
        // 在庫切れ件数
        // long outOfStockCount = products.stream()
        //         .filter(productService::isOutOfStock)
        //         .count();
        // // 残りわずか件数
        //         long lowStockCount = products.stream()
        //         .filter(productService::isLowStock)
        //         .count();
        // // 期限切れ件数
        //         long expiredCount = products.stream()
        //         .filter(productService::isPastExpiry)
        //         .count();
        // // 期限間近件数
        //         long nearExpiryCount = products.stream()
        //         .filter(productService::isNearExpiry)
        //         .count();

        // 空のMapを作成し、各集計値をあてはめる
        Map<String, Long> summaryMap = new HashMap<>();
        summaryMap.put("outOfStockCount", outOfStockCount);
        summaryMap.put("lowStockCount", lowStockCount);
        summaryMap.put("expiredCount", expiredCount);
        summaryMap.put("nearExpiryCount", nearExpiryCount);
        return summaryMap;
    }
}