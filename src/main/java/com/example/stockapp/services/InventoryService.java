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
    public List<Product> searchInventory(String keyword) {
        // キーワード未入力の場合は全件取得
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllInventory();
        }
        List<Product> products =productRepository
            .findByFamily_IdAndNameContainingAndIsDeletedFalseOrderByIdAsc(FAMILY_ID, keyword);
            return products;
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