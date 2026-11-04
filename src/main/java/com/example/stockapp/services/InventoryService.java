/*
 * 【役割】在庫一覧画面、アラート画面、ホーム画面の表示に必要なデータを取得・集計する「参照専用」のロジックを担当します。
 *
 * TODO: クラスに @Service と @Transactional(readOnly = true) を付与する
 * TODO: ホーム画面表示用の集計データをまとめるメソッドを実装する
 * TODO: 論理削除を除外した全在庫リストを取得するメソッドを実装する
 * TODO: アラート対象（在庫不足・期限切れ）の商品リストを取得するメソッドを実装する
 */
package com.example.stockapp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService {
    
    // 【処理内容】カテゴリ別一覧取得：すべて、または指定されたカテゴリIDに一致する商品を抽出する


    // 【処理内容】アラート対象抽出：「在庫切れ・残りわずか」や「期限切れ・期限まぢか」の条件に合致する商品を抽出する


    // 【処理内容】ホーム画面用集計：ホーム画面に表示する「在庫切れ件数」や「期限間近件数」をカウントして返す
}
