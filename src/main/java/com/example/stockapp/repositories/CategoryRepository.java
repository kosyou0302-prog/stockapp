/*
 * 【役割】カテゴリマスタテーブルに対するデータベース操作を担当します。
 * 基本的な全件取得ができれば十分なため、独自メソッドはひとまず不要です。
 *
 * TODO: JpaRepository<Category, Long> を継承する
 */
package com.example.stockapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{

}
