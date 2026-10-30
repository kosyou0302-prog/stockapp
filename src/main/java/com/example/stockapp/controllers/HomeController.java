/*
 * 【役割】アプリ起動時（URLのルート "/"）のアクセスを受け取り、最初のホーム画面を表示する窓口です。
 *
 * TODO: クラスに @Controller と @RequiredArgsConstructor を付与する
 * TODO: @GetMapping("/") を作成し、Serviceから集計データ（アラート件数など）を受け取る
 * TODO: 受け取ったデータを Model にセットし、"home"（home.html）を返す
 */
package com.example.stockapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // finalがついたフィールドを引数に持つコンストラクタをLombokで自動生成
public class HomeController {
    // ホーム-------------------------------------------------
    @GetMapping("/")
    public ModelAndView home(ModelAndView mav) {
        mav.setViewName("/home");
        return mav;
    }
}
