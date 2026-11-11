/*
 * 【役割】在庫一覧画面とアラート（期限切れ・在庫不足）画面の表示リクエストを受け取ります。
 * データの書き換えは行わず、データベースからの「参照」のみを行う安全なコントローラーです。
 *
 * TODO: クラスに @Controller と @RequiredArgsConstructor を付与する
 * TODO: @GetMapping("/list") で検索条件に合う在庫リストを取得し、Modelにセットして一覧画面を返す
 * TODO: @GetMapping("/alerts") でアラート対象リストを取得し、専用画面を返す
 */
package com.example.stockapp.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.stockapp.entities.Product;
import com.example.stockapp.services.InventoryService;
import com.example.stockapp.services.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // finalがついたフィールドを引数に持つコンストラクタをLombokで自動生成
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    // カテゴリー絞り込みボタン用のマスタデータを取得するService
    private final ProductService productService;

    // 在庫一覧---------------------------------------------------------------------------------------
    @GetMapping("/list")
    public ModelAndView inventory(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            ModelAndView mav) {

        // 検索文字の前後にある余分な空白を取り除く
        String searchKeyword = keyword.trim();

        // 商品名と選択中のカテゴリーを組み合わせて商品を取得する
        List<Product> products =
                inventoryService.searchInventory(searchKeyword, categoryId);

        // 検索結果の商品一覧をHTMLに渡す
        mav.addObject("products", products);

        // DBから取得したカテゴリー一覧を、絞り込みボタンの表示用に渡す
        mav.addObject("categories", productService.getMasterData().get("categories"));

        // 検索欄の文字と、カテゴリーの選択状態を維持する
        mav.addObject("keyword", searchKeyword);
        mav.addObject("selectedCategoryId", categoryId);

        mav.setViewName("inventory/list");
        return mav;
    }


    // 期限、在庫不足一覧------------------------------------------------------------------------------
    @GetMapping("/alerts")
    public ModelAndView alerts(ModelAndView mav) {

        List<Product> alertProducts =
            inventoryService.getAlertInventory();

        mav.addObject("alertProducts", alertProducts);
        mav.setViewName("inventory/alerts");
        return mav;
    }
}
