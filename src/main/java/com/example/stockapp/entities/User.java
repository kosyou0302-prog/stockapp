/*
 * 【役割】システムにログインする各ユーザーを管理するテーブルの設計図
 * ※初期開発フェーズでは実装を後回しとします。
 *
 * TODO: 後日、Spring Security導入時にカラム（ログインID、パスワード、Familyへの紐づけなど）を定義する
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
@Table(name = "app_users")
@Getter
@Setter

public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column
    private Long id;
}
