/*
 * 【役割】商品の新規登録、編集、論理削除など、データの「変更」を伴うリクエストを受け取ります。
 * 不正なデータがデータベースに入らないよう、入り口でバリデーション（入力チェック）を行います。
 *
 * TODO: クラスに @Controller, @RequiredArgsConstructor, @RequestMapping("/product") を付与する
 * TODO: 新規登録（GET/POST）と編集（GET/POST）のメソッドを作成する
 * TODO: @Validated を使ってフォームのエラーチェックを行い、成功時は一覧へリダイレクト（redirect:/inventory）させる
 * TODO: 削除ボタン押下時の論理削除用メソッド（@PostMapping("/delete/{id}")）を作成する
 */
package com.example.stockapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // finalがついたフィールドを引数に持つコンストラクタをLombokで自動生成
public class ProductController {
    // 新規登録---------------------------------------------------------------------------
    @GetMapping("product/add")
    public ModelAndView addItem(ModelAndView mav) {
        mav.setViewName("product/add");
        return mav;
    }
    @PostMapping("product/add")
    public ModelAndView add() {
        return new ModelAndView("redirect:/home");
    }

    // 商品編集,削除---------------------------------------------------------------------------
    @GetMapping("product/edit/{id}")
    public ModelAndView editItem(ModelAndView mav) {
        mav.setViewName("product/edit");
        return mav;
    }
    @PostMapping("product/edit/{id}")
    public ModelAndView edit() {
        return new ModelAndView("redirect:/home");
    }
}
