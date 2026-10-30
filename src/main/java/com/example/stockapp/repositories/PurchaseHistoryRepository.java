/*
 * 【役割】購入履歴テーブル（PurchaseHistory）に対するデータベース操作を担当します。
 *
 * TODO: JpaRepository<PurchaseHistory, Long> を継承する
 * TODO: 履歴を新しい順（降順）で取得するメソッドを定義する
 */
package com.example.stockapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.PurchaseHistory;

@Repository
public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    
}
