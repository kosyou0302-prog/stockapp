
/*
 * 【役割】購入履歴の記録と、画面表示用のデータ取得ロジックを担当します。
 * 購入処理（ShoppingListService）の中から呼び出されて、履歴データの生成を行います。
 *
 * TODO: クラスに @Service と @Transactional を付与する
 * TODO: 新しい購入履歴を保存するメソッドを実装する
 * TODO: 画面表示用に購入履歴一覧を新しい順で取得するメソッドを実装する
 */
package com.example.stockapp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseHistoryService {
    
    // 【処理内容】履歴の保存：「購入済みにする」操作が行われた際、対象商品のスナップショット（商品名・数量・購入日など）を保存する


    // 【処理内容】日付別グルーピング：取得した履歴データを、Javaの機能（Collectors.groupingByなど）を用いて日付ごとのMapに変換して返す
}
