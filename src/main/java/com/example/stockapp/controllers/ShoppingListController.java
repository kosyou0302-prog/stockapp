/*
 * 【役割】買い物リストの表示と、アイテムの手動追加・更新・一括購入リクエストを受け取ります。
 * JavaScript（Fetch API）を利用した非同期通信の受け口としての役割も持ちます。
 *
 * TODO: クラスに @Controller, @RequiredArgsConstructor, @RequestMapping("/shopping") を付与する
 * TODO: リスト表示、手動追加、数量変更、一括購入ボタン用の各メソッドを作成する
 * TODO: JSから呼ばれる「チェック状態の非同期更新」用に、@ResponseBody をつけた専用エンドポイントを作成する
 */
package com.example.stockapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // finalがついたフィールドを引数に持つコンストラクタをLombokで自動生成
@RequestMapping("/shopping")
public class ShoppingListController {
    // 買い物リスト表示--------------------------------------------------------------------
    @GetMapping("")
    public ModelAndView shop(ModelAndView mav) {
        mav.setViewName("shopping/list");
        return mav;
    }

    // 買い物リストに手動追加、削除---------------------------------------------------------------
    @PostMapping("")
    public ModelAndView add() {
        return new ModelAndView("redirect:/home");
    }

    // チェックボックス（JS）-------------------------------------------------------------------
    @PostMapping("/{id}")
    public ModelAndView update(ModelAndView mav) {
        return mav;
    }
}
