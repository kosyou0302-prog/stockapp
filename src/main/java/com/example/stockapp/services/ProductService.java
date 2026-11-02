/*
 * 【役割】商品の登録、更新、論理削除など、商品データに対する主要な変更ロジック（書き込み）を担当します。
 * Controllerから受け取ったフォームのデータをEntityに変換してRepositoryに保存を依頼します。
 *
 * TODO: クラスに @Service と @Transactional を付与する
 * TODO: 商品の新規登録処理（createProduct）を実装する
 * TODO: 商品の更新処理（updateProduct）を実装する
 * TODO: 商品の論理削除と、紐づく買い物リスト項目の連動削除処理（softDeleteProduct）を実装する
 */
package com.example.stockapp.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.stockapp.entities.Product;
import com.example.stockapp.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 期限間近を判定
    public void checkExpiringSoonProducts(Product product) {
        LocalDate startDate = product.getExpiryDate();
        LocalDate endDate = startDate.plusDays(product.getAlertDays());
    }
}
