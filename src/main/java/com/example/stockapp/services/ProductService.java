package com.example.stockapp.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockapp.dto.ProductForm;
import com.example.stockapp.entities.Category;
import com.example.stockapp.entities.Family;
import com.example.stockapp.entities.Icon;
import com.example.stockapp.entities.Product;
import com.example.stockapp.entities.Unit;
import com.example.stockapp.repositories.CategoryRepository;
import com.example.stockapp.repositories.FamilyRepository;
import com.example.stockapp.repositories.IconRepository;
import com.example.stockapp.repositories.ProductRepository;
import com.example.stockapp.repositories.ShoppingListItemRepository;
import com.example.stockapp.repositories.UnitRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private static final Long DUMMY_FAMILY_ID = 1L;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IconRepository iconRepository;
    private final UnitRepository unitRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final FamilyRepository familyRepository;
    private final ShoppingListService shoppingListService;

    /**
     * 【処理内容】Product1件データ取得（自家族・未削除のみ）
     */
    @Transactional(readOnly = true)
    public Product searchProduct(Long id) {
        return productRepository.findByIdAndFamily_IdAndIsDeletedFalse(id, DUMMY_FAMILY_ID)
                .orElseThrow(() -> new IllegalArgumentException("対象の商品が見つかりません"));
    }

    /**
     * 【処理内容】論理削除 : 指定IDの削除フラグを有効にし、関連する買い物リスト等のデータと連動させる
     */
    public void deleteProduct(Long id) {
        Product deleted = productRepository.findByIdAndFamily_IdAndIsDeletedFalse(id, DUMMY_FAMILY_ID)
                .orElseThrow(() -> new IllegalArgumentException("対象の商品が見つかりません"));
        deleted.setIsDeleted(true);
        productRepository.save(deleted);

        // 買い物リストの物理削除（連動）
        shoppingListItemRepository.deleteByProductId(id);
    }

    /**
     * 【処理内容】マスタ取得 : カテゴリや単位の全件リストを登録・編集画面のプルダウン用に提供する
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMasterData() {
        List<Category> categories = categoryRepository.findAll();
        List<Icon> icons = iconRepository.findAll();
        List<Unit> units = unitRepository.findAll();

        Map<String, Object> map = new HashMap<>();
        map.put("categories", categories);
        map.put("icons", icons);
        map.put("units", units);
        return map;
    }

    /**
     * 【処理内容】DTOから受け取ったデータを元に新規登録または更新を行う
     */
    public void saveFromForm(ProductForm form) {
        Product product;
        boolean isNew = (form.getId() == null);

        if (!isNew) {
            // 【更新の場合】家族ID=1 かつ 未削除 のデータのみ更新対象にする
            product = productRepository.findByIdAndFamily_IdAndIsDeletedFalse(form.getId(), DUMMY_FAMILY_ID)
                    .orElseThrow(() -> new IllegalArgumentException("対象の商品が見つかりません"));
        } else {
            // 【新規の場合】新しいインスタンスを作成し、初期値を設定
            product = new Product();
            product.setIsDeleted(false);

            // ログイン機能ができるまでの暫定対応：ID=1のダミー家族をセット
            Family dummyFamily = familyRepository.findById(DUMMY_FAMILY_ID)
                    .orElseThrow(() -> new IllegalStateException("ダミー家族が見つかりません"));
            product.setFamily(dummyFamily);
        }

        // フォームからの入力値をセット
        product.setName(form.getName());
        product.setStock(form.getStock());
        product.setThreshold(form.getThreshold());
        product.setIsNotify(Boolean.TRUE.equals(form.getIsNotify()));
        product.setExpiryDate(form.getExpiryDate());
        product.setAlertDays(form.getAlertDays());

        // カテゴリーの紐付け
        if (form.getCategory() != null) {
            Category realCategory = categoryRepository.findById(form.getCategory()).orElse(null);
            product.setCategory(realCategory);
        } else {
            product.setCategory(null);
        }

        // アイコンの紐付け
        if (form.getIcon() != null) {
            Icon realIcon = iconRepository.findById(form.getIcon()).orElse(null);
            product.setIcon(realIcon);
        } else {
            product.setIcon(null);
        }

        // 単位の紐付け
        if (form.getUnit() != null) {
            Unit realUnit = unitRepository.findById(form.getUnit()).orElse(null);
            product.setUnit(realUnit);
        } else {
            product.setUnit(null);
        }

        // 購入目安単位の紐付け
        if (form.getThresholdUnit() != null) {
            Unit realUnit = unitRepository.findById(form.getThresholdUnit()).orElse(null);
            product.setThresholdUnit(realUnit);
        } else {
            product.setThresholdUnit(null);
        }

        // データベースへ保存
        productRepository.save(product);

        // 買い物リストへの自動追加判定を実行
        shoppingListService.addAutoItem(product);
    }
}