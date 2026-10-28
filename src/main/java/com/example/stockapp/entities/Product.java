/*
 * 【役割】生活必需品（商品）の在庫状況や基本設定を管理するメインテーブルの設計図
 *
 * TODO: @Entity, @Id, @GeneratedValue などのJPAアノテーションを付与する
 * TODO: カラム（商品名、在庫数、アラート閾値、論理削除フラグ isDeleted など）を定義する
 * TODO: Category, Unit, Iconエンティティとのリレーション（@ManyToOne）を設定する
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
@Table(name = "products")
@Getter
@Setter

public class Product {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column
    private Long id;

    // 商品名
    @Column(length=20)
    private String name;

    // 在庫数
    @Column
    private Integer stock;

    // 使用期限
    @Column
    private LocalDate expiryDate;

    // 購入目安
    @Column
    private Integer threshold;

    // 通知判定
    @Column
    private Boolean isNotify;

    // 期限選択３or７
    @Column
    private Integer alertDays;

    // 家族名
    // @ManyToOne(optional= false, fetch= FetchType.LAZY)
    // @JoinColumn(name="family_id")
    // private Family family;

    // カテゴリ
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="categorys_id")
    private Category category;

    // 単位名
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="unit_id")
    private Unit unit;

    // アイコン
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="icon_id")
    private Icon icon;

}
