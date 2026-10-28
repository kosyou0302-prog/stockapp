/*
 * 【役割】「個」「ml」「g」などの単位を管理するマスタテーブルの設計図
 *
 * TODO: @Entity, @Id などのアノテーションを付与する
 * TODO: カラム（単位名）を定義する
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
@Table(name = "unit")
@Getter
@Setter

public class Unit {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column
    private Long id;

    // 単位名
    @Column(length=20)
    private String unit;
}
