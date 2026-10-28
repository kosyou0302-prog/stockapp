/*
 * 【役割】いつ、何を、いくつ買ったかの履歴を記録するテーブルの設計図
 *
 * TODO: @Entity, @Id などのアノテーションを付与する
 * TODO: カラム（購入日時、購入時の商品名、購入数量など）を定義する
 * TODO: ※商品が論理削除されても履歴名が残るよう、Productへの強い依存を持たせない設計を検討する
 */
package com.example.stockapp.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    // 商品名
    @Column(length=20)
    private String name;

    // 購入数
    @Column
    private Integer quantity;

    // 記入日
    @Column
    private LocalDate recordDate;

    // 家族名
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="family_id")
    private Family family;

    // 商品ID
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="product_id")
    private Product product;

}
