/*
 * 【役割】画面表示用のアイコン画像（パスやクラス名）を管理するマスタテーブルの設計図
 *
 * TODO: @Entity, @Id などのアノテーションを付与する
 * TODO: カラム（アイコン名、ファイルパスまたはCSSクラス名）を定義する
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
@Table(name = "icon")
@Getter
@Setter

public class Icon {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column
    private Long id;
}
