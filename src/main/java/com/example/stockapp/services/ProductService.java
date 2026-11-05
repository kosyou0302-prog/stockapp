/*
 * 【役割】商品の登録、更新、論理削除など、商品データに対する主要な変更ロジック（書き込み）を担当します。
 * Controllerから受け取ったフォームのデータをEntityに変換してRepositoryに保存を依頼します。
 *
 * TODO: クラスに @Service と @Transactional を付与する
 * TODO: 商品の新規登録処理（createProduct）を実装する
 * TODO: 商品の更新処理（updateProduct）を実装する
 * TODO: 商品の論理削除と、紐づく買い物リスト項目の連動削除処理（softDeleteProduct）を実装する
 */
package com.example.stockapp.services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IconRepository iconRepository;
    private final UnitRepository unitRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final FamilyRepository familyRepository;

    // 【処理内容】新規登録 : 受け取った商品情報を初期化（削除フラグ=false）して保存する
    @Transactional
    public void registerProduct(Product product) {
        // ログイン機能ができるまでの暫定対応：ID=1のダミー家族をセット
        Family dummyFamily = familyRepository.findById(1L).orElseThrow();
        product.setFamily(dummyFamily);
        // カテゴリー
        if(product.getCategory() != null && product.getCategory().getId() != null) {
            Long targetCategory = product.getCategory().getId();
            Category realCategory = categoryRepository.findById(targetCategory).orElseThrow();
            product.setCategory(realCategory);
        } else {
            // ユーザーが「未選択」にした場合
            product.setCategory(null);
        }
        // アイコン
        if(product.getIcon() != null && product.getIcon().getId() != null) {
            Long targetIconId = product.getIcon().getId();
            Icon realIcon = iconRepository.findById(targetIconId).orElseThrow();
            product.setIcon(realIcon);
        } else {
            // ユーザーが「未選択」にした場合
            product.setIcon(null);
        }
        // 単位
        if(product.getUnit() != null && product.getUnit().getId() != null) {
            Long targetUnit = product.getUnit().getId();
            Unit realUnit = unitRepository.findById(targetUnit).orElseThrow();
            product.setUnit(realUnit);
        } else {
            // ユーザーが「未選択」にした場合
            product.setUnit(null);
        }
        // 新規登録なので論理削除判定をfalseに設定
        product.setIsDeleted(false);
        productRepository.save(product);
    }

    // 【処理内容】更新 : 指定IDの既存データを取得し、新しい値で上書きして保存する
    @Transactional
    public void updateProduct(Product product) {
        // 画面から隠されている裏側のデータisDeleted等を守るため、いきなり保存せず、まずはDBから「変更前の古いデータ」を取得する
        Long targetId = product.getId();
        Product oldProduct = productRepository.findById(targetId).orElseThrow();
        // 画面からの変更を許可している項目だけを、古いデータに上書きする
        oldProduct.setName(product.getName());
        oldProduct.setStock(product.getStock());
        oldProduct.setExpiryDate(product.getExpiryDate());
        oldProduct.setThreshold(product.getThreshold());
        oldProduct.setIsNotify(product.getIsNotify());
        oldProduct.setAlertDays(product.getAlertDays());

        // マスタデータの紐づけ（不正なIDの保存を防ぐため、DBから本物を探してセットする）
        // カテゴリー
        if(product.getCategory() != null && product.getCategory().getId() != null) {
            Long targetCategory = product.getCategory().getId();
            Category realCategory = categoryRepository.findById(targetCategory).orElseThrow();
            oldProduct.setCategory(realCategory);
        } else {
            // ユーザーが「未選択」にした場合
            oldProduct.setCategory(null);
        }
        // アイコン
        if(product.getIcon() != null && product.getIcon().getId() != null) {
            Long targetIconId = product.getIcon().getId();
            Icon realIcon = iconRepository.findById(targetIconId).orElseThrow();
            oldProduct.setIcon(realIcon);
        } else {
            // ユーザーが「未選択」にした場合
            oldProduct.setIcon(null);
        }
        // 単位
        if(product.getUnit() != null && product.getUnit().getId() != null) {
            Long targetUnit = product.getUnit().getId();
            Unit realUnit = unitRepository.findById(targetUnit).orElseThrow();
            oldProduct.setUnit(realUnit);
        } else {
            // ユーザーが「未選択」にした場合
            oldProduct.setUnit(null);
        }
        productRepository.save(oldProduct);
    }

    // 【処理内容】論理削除 : 指定IDの削除フラグを有効にし、関連する買い物リスト等のデータと連動させる
    @Transactional
    public void deleteProduct(Long id) {
        Product deleted = productRepository.findById(id).orElseThrow();
        deleted.setIsDeleted(true);
        productRepository.save(deleted);

        // 買い物リストの物理削除（連動）
        shoppingListItemRepository.deleteByProductId(id);
    }

    // 【処理内容】マスタ取得 : カテゴリや単位の全件リストを登録・編集画面のプルダウン用に提供する
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

    // 【処理内容】期限間近を判定
    public boolean isExpired(Product product) {
        LocalDate startDate = product.getExpiryDate();
        LocalDate endDate = startDate.plusDays(product.getAlertDays());
        
    }

    // 【処理内容】残りわずかの判定
    public boolean isLowStock() {
    
    }
}
