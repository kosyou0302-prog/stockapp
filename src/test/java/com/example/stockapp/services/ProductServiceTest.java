package com.example.stockapp.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.example.stockapp.repositories.UnitRepository;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private FamilyRepository familyRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private IconRepository iconRepository;

    private Family testFamily;
    private Category testCategory;
    private Unit testUnit;
    private Icon testIcon;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        // 外部キー制約を満たすためのマスターデータ準備
        testFamily = new Family();
        testFamily = familyRepository.save(testFamily);

        testCategory = new Category();
        testCategory.setName("食品");
        testCategory = categoryRepository.save(testCategory);

        testUnit = new Unit();
        testUnit.setName("個");
        testUnit = unitRepository.save(testUnit);

        testIcon = new Icon();
        testIcon.setName("default.png");
        testIcon = iconRepository.save(testIcon);
    }

    // =========================================================================
    // UT-PRD-001: 商品の新規登録
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-001: 商品の新規登録が正常に行われ、初期状態で論理削除フラグがfalseになること")
    void utPrd001_registerProduct_ShouldSaveWithDefaultValues() {
        // ① 準備 (Given)
        Product product = new Product();
        product.setName("りんご");
        product.setStock(5);
        product.setThreshold(2);
        product.setCategory(testCategory);
        product.setUnit(testUnit);
        product.setThresholdUnit(testUnit);
        product.setIcon(testIcon);
        product.setIsNotify(true);

        // ② 実行 (When)
        productService.registerProduct(product);

        // ③ 検証 (Then)
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("りんご");
        assertThat(products.get(0).getIsDeleted()).isFalse(); // 初期値がfalse
        assertThat(products.get(0).getFamily()).isNotNull();
    }

    // =========================================================================
    // UT-PRD-002: 商品の更新
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-002: 既存商品の情報が指定した新しい値で正しく上書き更新されること")
    void utPrd002_updateProduct_ShouldOverwriteAllowedFields() {
        // ① 準備 (Given): 初期データを登録
        Product product = new Product();
        product.setName("みかん");
        product.setStock(3);
        product.setThreshold(1);
        product.setCategory(testCategory);
        product.setUnit(testUnit);
        product.setThresholdUnit(testUnit);
        product.setIcon(testIcon);
        product.setIsNotify(true);
        productService.registerProduct(product);

        Product savedProduct = productRepository.findAll().get(0);

        // 変更用のデータをセット
        savedProduct.setName("美味しいみかん");
        savedProduct.setStock(10);

        // ② 実行 (When)
        productService.updateProduct(savedProduct);

        // ③ 検証 (Then)
        Product updated = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("美味しいみかん");
        assertThat(updated.getStock()).isEqualTo(10);
    }

    // =========================================================================
    // UT-PRD-003: 論理削除と関連データの連動
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-003: 商品の削除を実行すると論理削除フラグがtrueになり、買い物リスト等の連動削除が行われること")
    void utPrd003_deleteProduct_ShouldLogicalDeleteAndRemoveShoppingList() {
        // ① 準備 (Given)
        Product product = new Product();
        product.setName("バナナ");
        product.setStock(4);
        product.setThreshold(1);
        product.setCategory(testCategory);
        product.setUnit(testUnit);
        product.setThresholdUnit(testUnit);
        product.setIcon(testIcon);
        product.setIsNotify(true);
        productService.registerProduct(product);

        Product savedProduct = productRepository.findAll().get(0);

        // ② 実行 (When)
        productService.deleteProduct(savedProduct.getId());

        // ③ 検証 (Then)
        Product deletedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(deletedProduct.getIsDeleted()).isTrue();
    }

    // =========================================================================
    // UT-PRD-004: 在庫切れ判定の検証
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-004: 在庫数が 0 の場合に isOutOfStock が true を返すこと")
    void utPrd004_isOutOfStock_ShouldReturnTrueWhenStockIsZero() {
        Product outOfStockProduct = new Product();
        outOfStockProduct.setStock(0);

        Product inStockProduct = new Product();
        inStockProduct.setStock(3);

        // 検証 (Then)
        assertThat(productService.isOutOfStock(outOfStockProduct)).isTrue();
        assertThat(productService.isOutOfStock(inStockProduct)).isFalse();
    }

    // =========================================================================
    // UT-PRD-005: 期限切れ判定の検証
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-005: 賞味期限が今日より過去の日付である場合に isPastExpiry が true を返すこと")
    void utPrd005_isPastExpiry_ShouldReturnTrueWhenPastDate() {
        Product expiredProduct = new Product();
        expiredProduct.setExpiryDate(LocalDate.now().minusDays(1)); // 昨日の日付

        Product validProduct = new Product();
        validProduct.setExpiryDate(LocalDate.now().plusDays(5)); // 未来の日付

        // 検証 (Then)
        assertThat(productService.isPastExpiry(expiredProduct)).isTrue();
        assertThat(productService.isPastExpiry(validProduct)).isFalse();
    }

    // =========================================================================
    // UT-PRD-006: 期限間近判定の検証
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-006: 賞味期限がアラート日数（alertDays）の範囲内である場合に isNearExpiry が true を返すこと")
    void utPrd006_isNearExpiry_ShouldReturnTrueWithinAlertDays() {
        LocalDate today = LocalDate.now();

        Product nearExpiryProduct = new Product();
        nearExpiryProduct.setExpiryDate(today.plusDays(2)); // 2日後が期限
        nearExpiryProduct.setAlertDays(3); // アラート期間は3日前から

        Product safeProduct = new Product();
        safeProduct.setExpiryDate(today.plusDays(10)); // 10日後（まだ余裕がある）
        safeProduct.setAlertDays(3);

        // 検証 (Then)
        assertThat(productService.isNearExpiry(nearExpiryProduct)).isTrue();
        assertThat(productService.isNearExpiry(safeProduct)).isFalse();
    }

    // =========================================================================
    // UT-PRD-007: 残りわずか判定の検証
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-007: 在庫数が 0より大きく閾値以下である場合に isLowStock が true を返すこと")
    void utPrd007_isLowStock_ShouldReturnTrueWhenStockIsLow() {
        Product lowStockProduct = new Product();
        lowStockProduct.setStock(1);
        lowStockProduct.setThreshold(2); // 閾値2以下なので「残りわずか」

        Product normalStockProduct = new Product();
        normalStockProduct.setStock(5);
        normalStockProduct.setThreshold(2); // 十分にある

        Product zeroStockProduct = new Product();
        zeroStockProduct.setStock(0);
        zeroStockProduct.setThreshold(2); // 0は在庫切れなので「残りわずか」ではない

        // 検証 (Then)
        assertThat(productService.isLowStock(lowStockProduct)).isTrue();
        assertThat(productService.isLowStock(normalStockProduct)).isFalse();
        assertThat(productService.isLowStock(zeroStockProduct)).isFalse();
    }

    // =========================================================================
    // UT-PRD-008: 商品1件取得の検証
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-008: 指定したIDに一致する商品データを正しく1件取得できること")
    void utPrd008_searchProduct_ShouldReturnProductById() {
        // ① 準備 (Given)
        Product product = new Product();
        product.setName("牛乳");
        product.setStock(2);
        product.setThreshold(1);
        product.setCategory(testCategory);
        product.setUnit(testUnit);
        product.setThresholdUnit(testUnit);
        product.setIcon(testIcon);
        product.setIsNotify(true);
        productService.registerProduct(product);

        Product savedProduct = productRepository.findAll().get(0);

        // ② 実行 (When)
        Product found = productService.searchProduct(savedProduct.getId());

        // ③ 検証 (Then)
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("牛乳");
    }

    // =========================================================================
    // UT-PRD-009: マスタデータ取得の検証
    // =========================================================================
    @Test
    @DisplayName("UT-PRD-009: 登録・編集画面用のマスターデータ（カテゴリ・単位・アイコン等）が正しく取得できること")
    void utPrd009_getMasterData_ShouldReturnMasterLists() {
        // ① 実行 (When)
        Map<String, Object> masterData = productService.getMasterData();

        // ② 検証 (Then): 各マスターデータのリストが含まれており、事前登録したデータが取得できていること
        assertThat(masterData).containsKey("categories");
        assertThat(masterData).containsKey("units");
        assertThat(masterData).containsKey("icons");

        @SuppressWarnings("unchecked")
        List<Category> categories = (List<Category>) masterData.get("categories");
        assertThat(categories).isNotEmpty();
    }
}
