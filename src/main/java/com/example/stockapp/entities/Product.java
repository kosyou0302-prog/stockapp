/*
 * 【役割】生活必需品（商品）の在庫状況や基本設定を管理するメインテーブルの設計図
 */
package com.example.stockapp.entities;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
<<<<<<< Updated upstream
import jakarta.validation.constraints.NotNull;
=======
>>>>>>> Stashed changes
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
    @NotBlank(message = "商品名を入力してください")
    @Column(length=20)
    private String name;

    // 在庫数
    @NotNull(message = "在庫数を入力してください")
    @Column
    private Integer stock;

    // 使用期限
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column
    private LocalDate expiryDate;

    // 購入目安
    @Column
    private Integer threshold;

    // 通知判定
    @Column
    private Boolean isNotify = true;

    // 期限選択３or７
    @Column
    private Integer alertDays;

    // 家族名
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="family_id")
    private Family family;

    // カテゴリ
    @NotNull(message = "分類を選択してください")
    @ManyToOne(optional= true, fetch= FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    // 単位名
    @ManyToOne(optional= false, fetch= FetchType.LAZY)
    @JoinColumn(name="unit_id")
    private Unit unit;

    // 購入目安単位
    @ManyToOne(optional= true, fetch= FetchType.LAZY)
    @JoinColumn(name="threshold_unit_id", nullable = true)
    private Unit thresholdUnit;

    // アイコン
    @ManyToOne(optional= true, fetch= FetchType.LAZY)
    @JoinColumn(name="icon_id")
    private Icon icon;

    // 論理削除
    @Column(nullable = false)
    private Boolean isDeleted = false;


    // 一覧表示の優先度判定
    public String getPrimaryAlert(LocalDate today) {
        // 通知OFF（false または null）の商品はアラート対象外
        if (!Boolean.TRUE.equals(isNotify)) {
            return "NONE";
        }
        String sAlert = stockAlert();
        String eAlert = expiryAlert(today);

        // 優先度１：在庫切れ
        if ("OUT_OF_STOCK".equals(sAlert)) {
            return "OUT_OF_STOCK";
        }
        // 優先度２・３：期限切れ・期限間近
        if (!"NONE".equals(eAlert)) {
            return eAlert;
        }
        // 優先度４：残り僅か
        if ("LOW_STOCK".equals(sAlert)) {
            return "LOW_STOCK";
        }

        return "NONE";
    }

    // 在庫切れ、残り僅か優先度判定
    public String stockAlert() {
        // 通知OFF（false または null）の商品はアラート対象外
        if (!Boolean.TRUE.equals(isNotify)) {
            return "NONE";
        }
        // 優先度１：在庫切れ
        // 商品がないため、使用期限が過ぎていても在庫切れだけを表示する
        if (stock != null && stock == 0) {
            return "OUT_OF_STOCK";
        }
        // 優先度２：残り僅か
        if (stock != null && threshold != null && 0 < stock && stock <= threshold) {
            return "LOW_STOCK";
        }
        return "NONE";
    }

    // 期限切れ、期限間近優先度判定
    public String expiryAlert(LocalDate today) {
        // 通知OFF（false または null）の商品はアラート対象外
        if (!Boolean.TRUE.equals(isNotify)) {
            return "NONE";
        }
        // 在庫がない・未設定の商品は期限アラートの対象外
        boolean hasStock = stock != null && stock > 0;
        // 優先度１：期限切れ
        if (hasStock && expiryDate != null && today.isAfter(expiryDate)) {
            return "EXPIRED";
        }
        // 優先度２：期限間近
        if (hasStock && expiryDate != null && alertDays != null
                && !today.isBefore(expiryDate.minusDays(alertDays))
                && !today.isAfter(expiryDate)) {
            return "EXPIRING_SOON";
        }
        return "NONE";
    }

    public String getPrimaryAlert(LocalDate today, String type) {
        if (type == null) {
            return getPrimaryAlert(today);
        }
        return switch (type) {
            case "stock", "out", "low" -> stockAlert();
            case "expiry", "expired", "soon" -> expiryAlert(today);
            default -> getPrimaryAlert(today);
        };
    }
}
