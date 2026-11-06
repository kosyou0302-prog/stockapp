/*
 * 【役割】商品テーブル（Product）に対するデータベース操作（CRUD）を担当します。
 *
 * TODO: JpaRepository<Product, Long> を継承したインターフェースにする
 * TODO: 「論理削除されていない（isDeletedがfalseの）全件」を取得するメソッドを定義する
 * TODO: 「在庫数が閾値以下」または「期限切れ」の商品を取得するアラート用メソッドを定義する
 */
package com.example.stockapp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 論理削除していない＝falseのものを全権取得 在庫一覧で削除以外を表示するため
    List<Product> findByIsDeletedFalse();

    // 在庫一覧あいうえお昇順
    List<Product> findByFamilyIdAndIsDeletedFalseOrderByNameAsc(Long familyId);

    // 在庫一覧あいまい検索機能（結果あいうえお昇順）
    List<Product> findByNameContainingAndFamilyIdAndIsDeletedFalseOrderByNameAsc(String keyword, Long familyId);

    // カテゴリ別商品一覧取得（商品名昇順）
    List<Product> findByCategoryIdAndFamilyIdAndIsDeletedFalseOrderByNameAsc(
            Long categoryId,
            Long familyId);

}
