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

    // 買い物データを物理的に削除するメソッド
    void deleteByProductId(Long productId);

    /*
     * 【処理内容】買い物リスト一覧の取得
     *
     * ・買い物リスト項目の家族IDが指定値と一致する
     * ・紐づく商品の家族IDも指定値と一致する
     * ・商品が論理削除されていない（isDeleted=false）
     * ・買い物リスト項目のIDが小さい順に並べる
     *
     * familyId：買い物リスト項目の家族ID
     * productFamilyId：商品の家族ID
     *
     * 戻り値：条件に一致した買い物リスト項目の一覧
     */
    List<ShoppingListItem>
        findByFamilyIdAndProductFamilyIdAndProductIsDeletedFalseOrderByIdAsc(
            Long familyId,
            Long productFamilyId);

    /*
     * 【処理内容】同じ家族・同じ商品の登録済み項目を検索する
     *
     * 手動追加：
     * 登録済みなら、既存の購入予定数へ加算する。
     *
     * 自動追加：
     * 登録済みなら、追加しない。
     *
     * 戻り値：該当項目があればその項目、なければ空のOptional
     * ※同じ家族・同じ商品につき1件の運用を前提とする。
     */
    Optional<ShoppingListItem> findByFamilyIdAndProductId(
            Long familyId,
            Long productId);

    /*
     * 【処理内容】購入完了の対象となるチェック済み項目を取得する
     *
     * ・買い物リスト項目と商品の家族IDが指定値と一致する
     * ・チェック状態がtrue
     * ・商品が論理削除されていない
     * ・買い物リスト項目のIDが小さい順に並べる
     */
    List<ShoppingListItem>
        findByFamilyIdAndProductFamilyIdAndIsCheckedTrueAndProductIsDeletedFalseOrderByIdAsc(
            Long familyId,
            Long productFamilyId);


}
