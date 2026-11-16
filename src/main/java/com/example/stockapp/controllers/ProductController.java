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
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.stockapp.entities.Icon;
import com.example.stockapp.entities.Product;
import com.example.stockapp.entities.Unit;
import com.example.stockapp.services.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



/*
 * 【役割】商品の新規登録、編集、論理削除など、データの「変更」を伴うリクエストを受け取ります。
 * バリデーションによる入力チェックを行い、Service層へ処理を委譲します。
 */
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 商品登録画面を表示する (GET /product/add)
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Product product = new Product();

        // アイコンの初期値：リンゴ（1.pngに対応するアイコンID）
        // プレビュー表示だけでなく、送信するicon.idにも初期値を設定する
        Icon icon = new Icon();
        icon.setId(1L);
        product.setIcon(icon);

        // 在庫数の単位の初期値：「個」（単位ID＝1）
        Unit unit = new Unit();
        unit.setId(1L);
        product.setUnit(unit);

        // 購入目安単位の初期値：「個」（単位ID＝1）
        Unit thresholdUnit = new Unit();
        thresholdUnit.setId(1L);
        product.setThresholdUnit(thresholdUnit);

        // 初期値を設定した商品を登録画面へ渡す
        // 通知はProductのisNotify = trueにより、初期状態でONになる
        model.addAttribute("item", product);

        // プルダウン等のマスタデータを取得して渡す
        model.addAllAttributes(productService.getMasterData());
        return "product/add";
    }

    /**
     *  商品登録処理を実行する (POST /product/add)
     */
    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute("item") Product product,
                            BindingResult bindingResult,
                            Model model) {
        // 入力エラーがある場合は登録画面に戻る
        if (bindingResult.hasErrors()) {
            model.addAllAttributes(productService.getMasterData());
            return "product/add";
        }

        // 登録処理の実行
        productService.registerProduct(product);

        // 在庫一覧画面へリダイレクト
        return "redirect:/inventory/list";
    }

    /**
     * 商品編集画面を表示する (GET /product/edit/{id})
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, 
                            Model model, 
                            RedirectAttributes redirectAttributes) {
        try {
            // 編集対象のデータを検索
            Product product = productService.searchProduct(id);
            model.addAttribute("item", product);
            model.addAllAttributes(productService.getMasterData());
            return "product/edit";
        } catch (Exception e) {
            // データが見つからない場合はメッセージを持たせて一覧へリダイレクト
            redirectAttributes.addFlashAttribute("errorMessage", "データが見つかりません");
            return "redirect:/inventory/list";
        }
    }

    /**
     * 商品更新処理を実行する (POST /product/edit)
     */
    @PostMapping("/edit")
    public String updateProduct(@Valid @ModelAttribute("item") Product product,
                            BindingResult bindingResult,
                            Model model) {
        // 入力エラーがある場合は編集画面に戻る
        if (bindingResult.hasErrors()) {
            model.addAllAttributes(productService.getMasterData());
            return "product/edit";
        }

        // 更新処理の実行
        productService.updateProduct(product);

        // 在庫一覧画面へリダイレクト
        return "redirect:/inventory/list";
    }

    /**
     * 商品削除処理を実行する (POST /product/delete/{id})
     */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, 
                            RedirectAttributes redirectAttributes) {
        try {
            //削除の実行（関連する買い物リストの削除もService内で連動）
            productService.deleteProduct(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "データが見つかりません");
        }

        // 在庫一覧画面へリダイレクト
        return "redirect:/inventory/list";
    }
}