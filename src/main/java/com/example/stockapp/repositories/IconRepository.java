/*
 * 【役割】アイコンマスタテーブルに対するデータベース操作を担当します。
 *
 * TODO: JpaRepository<Icon, Long> を継承する
 */
package com.example.stockapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.Icon;

@Repository
public interface IconRepository extends JpaRepository<Icon, Long> {
    
}
