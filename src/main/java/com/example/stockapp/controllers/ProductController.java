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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.stockapp.dto.ProductForm;
import com.example.stockapp.entities.Product;
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
    public ModelAndView showAddForm(ModelAndView mav) {
        ProductForm form = new ProductForm();
        form.setIcon(1L);
        form.setUnit(1L);
        form.setThresholdUnit(1L);
        form.setIsNotify(true);

        // "item" という名前でビューに渡す
        mav.addObject("item", form);
        mav.addAllObjects(productService.getMasterData());
        mav.setViewName("product/add");
        return mav;
    }

    /**
     *  商品登録処理を実行する (POST /product/add)
     */
    @PostMapping("/add")
    public ModelAndView addProduct(@Valid @ModelAttribute("item") ProductForm form,BindingResult result,ModelAndView mav) {
        if (result.hasErrors()) {
            mav.addObject("item", form);
            mav.addAllObjects(productService.getMasterData());
            mav.setViewName("product/add");
            return mav;
        }
        productService.saveFromForm(form);
        mav.setViewName("redirect:/inventory/list");
        return mav;
    }

    /**
     * 商品編集画面を表示する (GET /product/edit/{id})
     */
    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable("id") Long id,ModelAndView mav,RedirectAttributes redirectAttributes) {
        try {
            // 編集対象のデータを検索してProductFormに詰め替える
            Product product = productService.searchProduct(id);
            ProductForm form = new ProductForm();
            form.setId(product.getId());
            form.setName(product.getName());
            form.setStock(product.getStock());
            form.setThreshold(product.getThreshold());
            form.setIsNotify(product.getIsNotify());
            form.setExpiryDate(product.getExpiryDate());
            form.setAlertDays(product.getAlertDays());

            if (product.getCategory() != null) form.setCategory(product.getCategory().getId());
            if (product.getIcon() != null) form.setIcon(product.getIcon().getId());
            if (product.getUnit() != null) form.setUnit(product.getUnit().getId());
            if (product.getThresholdUnit() != null) form.setThresholdUnit(product.getThresholdUnit().getId());

            mav.addObject("item", form);
            mav.addAllObjects(productService.getMasterData());
            mav.setViewName("product/edit");
            return mav;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "データが見つかりません");
            mav.setViewName("redirect:/inventory/list");
            return mav;
        }
    }

    /**
     * 商品更新処理を実行する (POST /product/edit)
     */
    @PostMapping("/edit")
    public ModelAndView editProduct(@Valid @ModelAttribute("item") ProductForm form,BindingResult result,ModelAndView mav) {
        if (result.hasErrors()) {
            mav.addObject("item", form);
            mav.addAllObjects(productService.getMasterData());
            mav.setViewName("product/edit");
            return mav;
        }
        productService.saveFromForm(form);
        mav.setViewName("redirect:/inventory/list");
        return mav;
    }

    /**
     * 商品削除処理を実行する (POST /product/delete/{id})
     */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "データが見つかりません");
        }
        return "redirect:/inventory/list";
    }
}