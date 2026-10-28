/*
 * 【役割】単位マスタテーブルに対するデータベース操作を担当します。
 *
 * TODO: JpaRepository<Unit, Long> を継承する
 */
package com.example.stockapp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.stockapp.entities.Unit;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    
}
