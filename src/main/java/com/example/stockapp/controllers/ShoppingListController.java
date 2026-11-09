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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.stockapp.services.InventoryService;
import com.example.stockapp.services.ShoppingListService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // finalがついたフィールドを引数に持つコンストラクタをLombokで自動生成
@RequestMapping("/shopping")
public class ShoppingListController {

    // 買い物リスト一覧を取得するService
    private final ShoppingListService shoppingListService;

    // 手動追加フォームの商品選択肢を取得するService
    private final InventoryService inventoryService;

    // 買い物リスト表示--------------------------------------------------------------------
    @GetMapping("/list")
    public ModelAndView shop(ModelAndView mav) {
        // 現在の家族の買い物リストを取得し、HTMLに渡す
        mav.addObject(
                "shoppingItems",
                shoppingListService.getShoppingList());

        // 手動追加フォームで選択できる商品一覧を取得し、HTMLに渡す
        mav.addObject(
                "products",
                inventoryService.getAllInventory());

        mav.setViewName("shopping/list");
        return mav;
    }

    // 買い物リストに手動追加＆自動追加、削除---------------------------------------------------------------
    /*
    * 【処理内容】買い物リストへの手動追加
    *   productId：画面で選択した商品のID
    *   quantity ：画面で入力した追加数量
    *
    * 削除処理は、今後別のメソッドで実装する。
    */
    @PostMapping("/list")
    @Transactional
    public ModelAndView add(
                @RequestParam("productId") Long productId,
                @RequestParam("quantity") Integer quantity) {

    // Serviceへ商品IDと追加数量を渡し、買い物リストに追加する
    shoppingListService.addManualItem(productId, quantity);

    // 追加後は買い物リスト画面へ戻り、更新された一覧を表示する
    return new ModelAndView("redirect:/shopping/list");

        //return new ModelAndView("redirect:/home");
    }

    // チェックボックス（JS）-------------------------------------------------------------------
    @ResponseBody
    @PostMapping("/list/{id}")
    @Transactional
    public Integer update(@PathVariable("id") Long id) {
        return 1; //仮
    }
}