/*
 * 【役割】在庫一覧画面とアラート（期限切れ・在庫不足）画面の表示リクエストを受け取ります。
 * データの書き換えは行わず、データベースからの「参照」のみを行う安全なコントローラーです。
 *
 * TODO: クラスに @Controller と @RequiredArgsConstructor を付与する
 * TODO: @GetMapping("/inventory") で全在庫リストを取得し、Modelにセットして一覧画面を返す
 * TODO: @GetMapping("/inventory/alerts") でアラート対象リストを取得し、専用画面を返す
 */
package com.example.stockapp.controllers;

public class InventoryController {
    
}
