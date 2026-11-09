/*
 * 【役割】買い物リストテーブル（ShoppingListItem）に対するデータベース操作を担当します。
 *
 * TODO: JpaRepository<ShoppingListItem, Long> を継承する
 * TODO: 対象の商品IDに紐づく買い物リストの項目を削除するメソッド（deleteByProductIdなど）を定義する

 * 【実装済み】
 * ・JpaRepositoryの継承
 * ・商品IDに紐づく買い物リスト項目の削除
 * ・家族IDで絞り込んだ買い物リスト一覧の取得
 */

package com.example.stockapp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.ShoppingListItem;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    /**
     * 【物理削除】商品に紐づく買い物リスト項目を削除します。
     * 商品マスタが削除された場合の整合性保持などに使用します。
     *
     * @param productId 削除対象の商品ID
     */
    void deleteByProductId(Long productId);

    /**
     * 【一覧取得】指定した家族の買い物リスト一覧を取得します。
     * 論理削除された商品は除外され、IDの昇順で並びます。
     *
     * @param familyId 買い物リスト項目の家族ID
     * @param productFamilyId 商品マスタ側の家族ID
     * @return 条件に一致した買い物リスト項目の一覧
     */
    List<ShoppingListItem>
        findByFamily_IdAndProduct_Family_IdAndProduct_IsDeletedFalseOrderByIdAsc(
            Long familyId, Long productFamilyId);

    /**
     * 【重複確認】同一家族内で特定の商品が既にリストへ登録されているかを検索します。
     *
     * ・手動追加：登録済みの場合は購入予定数量を加算
     * ・自動追加：登録済みの場合は二重登録防止のためスキップ
     *
     * @param familyId 家族ID
     * @param productId 商品ID
     * @return 該当する項目（存在しない場合は空の Optional）
     */
    Optional<ShoppingListItem> findByFamily_IdAndProduct_Id(
            Long familyId, Long productId);

    /**
     * 【購入対象抽出】指定した家族の買い物リストから、チェックが付いている（isChecked = true）項目を取得します。
     * 一括購入処理（completePurchase）の在庫加算・リスト削除対象として使用します。
     *
     * @param familyId 家族ID
     * @return チェック済みの買い物リスト項目一覧
     */
    List<ShoppingListItem> findByFamily_IdAndIsCheckedTrue(Long familyId);
}
