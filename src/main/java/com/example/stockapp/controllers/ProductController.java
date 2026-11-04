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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.example.stockapp.entities.Product;
import com.example.stockapp.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // finalがついたフィールドを引数に持つコンストラクタをLombokで自動生成
@RequestMapping("/product")
public class ProductController {
    private final ProductRepository productRepository;
    // 新規登録---------------------------------------------------------------------------
    @GetMapping("/add")
    public ModelAndView addItem(ModelAndView mav) {
        mav.addObject("item", new Product());
        mav.setViewName("product/add");
        return mav;
    }
    @PostMapping("/add")
    public ModelAndView add() {
        return new ModelAndView("redirect:/home");
    }

    // 商品編集,削除---------------------------------------------------------------------------
    @GetMapping("/edit/{id}")
    public ModelAndView editItem(@PathVariable Long id, ModelAndView mav) {
        mav.setViewName("product/edit");
        Product data = productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "データが見つかりません"));
        mav.addObject("item",data);
        return mav;
    }
    @PostMapping("/edit/{id}")
    public ModelAndView edit(@ModelAttribute("item") @Validated Product product,BindingResult result) {
        productRepository.saveAndFlush(product);
        return new ModelAndView("redirect:/home");
    }
}
