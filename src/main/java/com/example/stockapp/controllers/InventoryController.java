/*
 * 【役割】在庫一覧画面とアラート（期限切れ・在庫不足）画面の表示リクエストを受け取ります。
 * データの書き換えは行わず、データベースからの「参照」のみを行う安全なコントローラーです。
 *
 * TODO: クラスに @Controller と @RequiredArgsConstructor を付与する
 * TODO: @GetMapping("/inventory") で全在庫リストを取得し、Modelにセットして一覧画面を返す
 * TODO: @GetMapping("/inventory/alerts") でアラート対象リストを取得し、専用画面を返す
 */
package com.example.stockapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // finalがついたフィールドを引数に持つコンストラクタをLombokで自動生成
@RequestMapping("/inventory")
public class InventoryController {
    // 在庫一覧---------------------------------------------------------------------------------------
    @GetMapping("list")
    public ModelAndView inventory(ModelAndView mav) {
        mav.setViewName("inventory/list");
        return mav;
    }



    // 期限、在庫不足一覧------------------------------------------------------------------------------
    @GetMapping("alerts")
    public ModelAndView alerts(ModelAndView mav) {
        mav.setViewName("inventory/alerts");
        return mav;
    }
}
