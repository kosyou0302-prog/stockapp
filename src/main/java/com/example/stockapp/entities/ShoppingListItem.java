/*
 * 【役割】買い物リスト画面に表示される購入予定アイテムを管理するテーブルの設計図
 *
 * TODO: @Entity, @Id などのアノテーションを付与する
 * TODO: カラム（購入予定数量、チェック状態 isChecked など）を定義する
 * TODO: Productエンティティとのリレーション（@ManyToOne）を設定し、どの商品を買うのか紐づける
 */
package com.example.stockapp.entities;

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
@Table(name = "shopping_listitems")
@Getter
@Setter

public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column
    private Long id;

    // 買う予定数
    @Column
    private Integer threshold;

    // 家族名
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="family_id")
    private Family family;

    // 商品ID
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="product_id")
    private Product product;

    // チェック状態
    @Column
    private Boolean isChecked;
}
