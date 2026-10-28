/*
 * 【役割】データ共有の単位となる「家族（グループ）」を管理するテーブルの設計図で
 * ※初期開発フェーズではダミーID（1Lなど）を使用するため、実装は後回しとします。
 *
 * TODO: 後日、ログイン機能追加時にカラム（家族名、登録日時など）を定義する
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
@Table(name = "family")
@Getter
@Setter

public class Family {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column
    private Long id;
}
