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
import org.springframework.transaction.annotation.Transactional;

import com.example.stockapp.entities.Product;
import com.example.stockapp.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 【処理内容】新規登録 : 受け取った商品情報を初期化（削除フラグ=false）して保存する
    public void registerProduct() {
    
    }

    // 【処理内容】更新 : 指定IDの既存データを取得し、新しい値で上書きして保存する
    public void updateProduct() {
    
    }

    // 【処理内容】論理削除 : 指定IDの削除フラグを有効にし、関連する買い物リスト等のデータと連動させる
    public void deleteProduct() {
    
    }

    // 【処理内容】マスタ取得 : カテゴリや単位の全件リストを登録・編集画面のプルダウン用に提供する
    public void getMasterData() {
    
    }

    // 期限間近を判定
    public void isExpired(Product product) {
        LocalDate startDate = product.getExpiryDate();
        LocalDate endDate = startDate.plusDays(product.getAlertDays());
        
    }

    // 残りわずかの判定
    public void isLowStock() {
    
    }
}
