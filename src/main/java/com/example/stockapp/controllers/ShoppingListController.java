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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.stockapp.entities.ShoppingListItem;
import com.example.stockapp.repositories.ShoppingListItemRepository;
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

  //在庫情報や商品情報の参照・検索
    private final ShoppingListItemRepository shoppingListItemRepository;


    // 買い物リスト表示-------------------------------------------------------------
    @GetMapping("/list")
    public ModelAndView shop(ModelAndView mav) {
        // 現在の家族の買い物リストを取得、HTMLに渡す
        mav.addObject("shoppingItems", shoppingListService.getShoppingList());
        // 手動追加用の商品一覧を取得、HTMLに渡す
        mav.addObject("products", inventoryService.getAllInventory());
        // カテゴリー一覧を取得（list_2.htmlのカテゴリー絞り込み用）
        mav.addObject("categories", productServiceGetCategories()); // ※InventoryService経由等で取得

        mav.setViewName("shopping/list");
        return mav;
    }

    // 買い物リストに手動追加＆自動追加、削除------------------------------------
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
    }

    // チェック状態の非同期更新（JS）------------------------------------
    @ResponseBody
    @PostMapping("/list/{id}")
    @Transactional
    public String updateCheckStatus(@PathVariable("id") Long id) {
        ShoppingListItem item = shoppingListItemRepository.findById(id).orElse(null);
        if (item != null) {
            // 現在の状態を反転させて保存
            boolean nextState = !Boolean.TRUE.equals(item.getIsChecked());
            item.setIsChecked(nextState);
            shoppingListItemRepository.save(item);
            return "success";
        }
        return "error";
    }

    // 数量の増減処理 -------------------------------------------------------------
    @PostMapping("/quantity/{id}")
    @Transactional
    public ModelAndView updateQuantity(
            @PathVariable("id") Long id,
            @RequestParam("change") int change) {
        ShoppingListItem item = shoppingListItemRepository.findById(id).orElse(null);
        if (item != null) {
            int currentQty = item.getThreshold() != null ? item.getThreshold() : 1;
            int newQty = currentQty + change;
            // 数量が1未満にならないように制限
            if (newQty < 1) {
                newQty = 1;
            }
            item.setThreshold(newQty);
            shoppingListItemRepository.save(item);
        }
        return new ModelAndView("redirect:/shopping/list");
    }

    //  チェックした商品の削除-------------------------------------------------------------
    @PostMapping("/delete")
    @Transactional
    public ModelAndView deleteCheckedItems() {
        // チェックされている（isChecked = true）項目をすべて取得して削除
        var checkedItems = shoppingListItemRepository.findByFamily_IdAndIsCheckedTrue(1L); // ダミー家族ID 1L
        if (!checkedItems.isEmpty()) {
            shoppingListItemRepository.deleteAll(checkedItems);
        }
        return new ModelAndView("redirect:/shopping/list");
    }

    //  チェック済み商品の一括購入完了----------------------------------------------
    @PostMapping("/complete")
    @Transactional
    public ModelAndView completePurchase(RedirectAttributes redirectAttributes) {
        int completedCount = shoppingListService.completePurchase();
        redirectAttributes.addFlashAttribute("message", completedCount + "件の商品を購入完了しました。");
        return new ModelAndView("redirect:/shopping/list");
    }

    // ※カテゴリ一覧取得のヘルパー（InventoryService等から取得する前提のプレースホルダー）
    private Object productServiceGetCategories() {
        return inventoryService.getAllInventory(); // またはマスタ取得メソッドへ置き換え
    }
}