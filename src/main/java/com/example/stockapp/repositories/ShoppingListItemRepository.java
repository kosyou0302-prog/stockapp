/*
 * 【役割】買い物リストテーブル（ShoppingListItem）に対するデータベース操作を担当します。
 *
 * TODO: JpaRepository<ShoppingListItem, Long> を継承する
 * TODO: 対象の商品IDに紐づく買い物リストの項目を削除するメソッド（deleteByProductIdなど）を定義する
 */
package com.example.stockapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.ShoppingListItem;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

}
