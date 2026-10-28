/*
 * 【役割】いつ、何を、いくつ買ったかの履歴を記録するテーブルの設計図
 *
 * TODO: @Entity, @Id などのアノテーションを付与する
 * TODO: カラム（購入日時、購入時の商品名、購入数量など）を定義する
 * TODO: ※商品が論理削除されても履歴名が残るよう、Productへの強い依存を持たせない設計を検討する
 */
package com.example.stockapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "purchase_history")
@Getter
@Setter

public class PurchaseHistory {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column
    private Long id;
}
