/*
 * 【役割】商品登録・編集画面（HTMLのフォーム）から送信された入力データを受け取る専用の「箱」です。
 * データベースの設計図（Entity）とは意図的に切り離し、ここで入力値の厳密なチェック（バリデーション）を行います。
 *
 * TODO: クラスに Lombok の @getter @setterアノテーションを付与し、ゲッター/セッターを自動生成する
 * TODO: 画面の入力項目（商品名、在庫数、アラート閾値など）と一致する変数を定義する
 * TODO: 不正なデータ弾くため、変数に @NotBlank（空文字禁止）や @Min(0)（0以上）などのバリデーションアノテーションをつける
 */
package com.example.stockapp.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductForm {

    private Long id;
    // 商品名
    @NotBlank(message = "商品名を入力してください")
    private String name;

    // 分類
    @NotBlank(message = "分類を選択してください")
    private String category;

    // 在庫数
    @NotNull(message = "在庫数を入力してください")
    @Min(value = 0, message = "0以上で入力してください")
    @Max(value = 50, message = "数を確認してください")
    private Integer stock;

    // 単位
    @NotBlank(message = "単位を選択してください")
    private String unit;

    // 購入目安
    @NotNull(message = "購入目安を入力してください")
    @Min(value = 0, message = "0以上で入力してください")
    @Max(value = 50, message = "数を確認してください")
    private Integer threshold;

    // 購入目安単位
    @NotBlank(message = "単位を選択してください")
    private String thresholdUnit;

    // 使用期限
    @NotNull(message = "使用期限を入力してください")
    @FutureOrPresent(message = "使用期限は本日以降の日付を入力してください")
    private LocalDate expiryDate;

    // 通知判定　通知ON時をデフォルト設定
    private Boolean isNotify = false;

    // 期限選択（アラート日数）　　要追記
    @NotNull(message = "通知日数を選択してください")
    private Integer alertDays;

    /* 通知日数は 1・3・7 のみ許可 */
    @AssertTrue(message = "通知日数は1日・3日・7日から選択してください")
    public boolean isValidAlertDays() {
        return alertDays == null
                || alertDays == 3
                || alertDays == 7;
    }
}