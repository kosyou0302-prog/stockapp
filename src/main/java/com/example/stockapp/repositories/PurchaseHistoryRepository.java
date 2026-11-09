/*
 * 【役割】購入履歴テーブル（PurchaseHistory）に対するデータベース操作を担当します。
 */
package com.example.stockapp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.PurchaseHistory;

@Repository
public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    /**
     * 【一覧取得】指定した家族の購入履歴一覧を、購入日の新しい順（降順）で取得します。
     * 同一購入日内の場合は、IDの降順（新しく登録された順）で並び替えます。
     *
     * @param familyId 家族ID
     * @return 整列済みの購入履歴リスト
     */
    List<PurchaseHistory> findByFamily_IdOrderByRecordDateDescIdDesc(Long familyId);
}
