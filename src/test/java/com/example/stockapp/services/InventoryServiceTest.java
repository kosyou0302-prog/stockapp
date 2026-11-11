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
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;
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
    private Category testCategory1;
    private Category testCategory2;
    private Unit testUnit;
    private Icon testIcon;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        // プレースホルダー（FAMILY_ID = 1L）に対応するFamilyエンティティを作成
        testFamily = new Family();
        testFamily.setId(1L); // IDを 1L に固定
        testFamily = familyRepository.save(testFamily);

        testCategory1 = new Category();
        testCategory1.setName("食品");
        testCategory1 = categoryRepository.save(testCategory1);

        testCategory2 = new Category();
        testCategory2.setName("日用品");
        testCategory2 = categoryRepository.save(testCategory2);

        testUnit = new Unit();
        testUnit.setName("個");
        testUnit = unitRepository.save(testUnit);

        testIcon = new Icon();
        testIcon.setName("default.png");
        testIcon = iconRepository.save(testIcon);
    }

    // =========================================================================
    // UT-INV-001: 全在庫一覧取得（論理削除除外）
    // =========================================================================
    @Test
    @DisplayName("UT-INV-001: 論理削除されていない商品のみが全件取得できること")
    void utInv001_getAllInventory_ShouldReturnActiveProductsOnly() {
        // ① 準備 (Given): 有効な商品を作成
        Product activeProduct = new Product();
        activeProduct.setName("りんご");
        activeProduct.setFamily(testFamily);
        activeProduct.setStock(5);
        activeProduct.setThreshold(2);
        activeProduct.setCategory(testCategory1);
        activeProduct.setUnit(testUnit);
        activeProduct.setThresholdUnit(testUnit);
        activeProduct.setIcon(testIcon);
        activeProduct.setIsDeleted(false);
        activeProduct.setIsNotify(true);
        productRepository.save(activeProduct);

        // 準備 (Given): 論理削除された商品を作成
        Product deletedProduct = new Product();
        deletedProduct.setName("みかん");
        deletedProduct.setFamily(testFamily); // 修正：正しく deletedProduct にセット
        deletedProduct.setStock(3);
        deletedProduct.setThreshold(1);
        deletedProduct.setCategory(testCategory1);
        deletedProduct.setUnit(testUnit);
        deletedProduct.setThresholdUnit(testUnit);
        deletedProduct.setIcon(testIcon);
        deletedProduct.setIsDeleted(true); // 論理削除済み
        deletedProduct.setIsNotify(true);
        productRepository.save(deletedProduct);

        // ② 実行 (When)
        List<Product> result = inventoryService.getAllInventory();

        // ③ 検証 (Then): 有効な商品（りんご）の1件のみ取得されること
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("りんご");
    }

    // =========================================================================
    // UT-INV-002: 商品名によるあいまい検索
    // =========================================================================
    @Test
    @DisplayName("UT-INV-002: キーワードを用いた部分一致検索が正しく機能し、未入力時は全件取得されること")
    void utInv002_searchInventory_ShouldReturnFilteredOrAllProducts() {
        Product p1 = new Product();
        p1.setName("牛乳");
        p1.setFamily(testFamily);
        p1.setStock(2);
        p1.setThreshold(1);
        p1.setCategory(testCategory1);
        p1.setUnit(testUnit);
        p1.setThresholdUnit(testUnit);
        p1.setIcon(testIcon);
        p1.setIsDeleted(false);
        p1.setIsNotify(true);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("コーヒー牛乳");
        p2.setFamily(testFamily);
        p2.setStock(1);
        p2.setThreshold(1);
        p2.setCategory(testCategory1);
        p2.setUnit(testUnit);
        p2.setThresholdUnit(testUnit);
        p2.setIcon(testIcon);
        p2.setIsDeleted(false);
        p2.setIsNotify(true);
        productRepository.save(p2);

        List<Product> searchResult = inventoryService.searchInventory("牛乳");
        assertThat(searchResult).hasSize(2);

        List<Product> emptyResult = inventoryService.searchInventory(null);
        assertThat(emptyResult).hasSize(2);
    }

    // =========================================================================
    // UT-INV-003: カテゴリ別一覧取得
    // =========================================================================
    @Test
    @DisplayName("UT-INV-003: 指定したカテゴリIDに一致する在庫一覧が取得でき、null指定時は全件返ること")
    void utInv003_getInventoryByCategory_ShouldFilterByCategory() {
        Product pFood = new Product();
        pFood.setName("パン");
        pFood.setFamily(testFamily);
        pFood.setStock(2);
        pFood.setThreshold(1);
        pFood.setCategory(testCategory1);
        pFood.setUnit(testUnit);
        pFood.setThresholdUnit(testUnit);
        pFood.setIcon(testIcon);
        pFood.setIsDeleted(false);
        pFood.setIsNotify(true);
        productRepository.save(pFood);

        Product pDaily = new Product();
        pDaily.setName("洗剤");
        pDaily.setFamily(testFamily);
        pDaily.setStock(1);
        pDaily.setThreshold(1);
        pDaily.setCategory(testCategory2);
        pDaily.setUnit(testUnit);
        pDaily.setThresholdUnit(testUnit);
        pDaily.setIcon(testIcon);
        pDaily.setIsDeleted(false);
        pDaily.setIsNotify(true);
        productRepository.save(pDaily);

        List<Product> foodList = inventoryService.getInventoryByCategory(testCategory1.getId());
        assertThat(foodList).hasSize(1);
        assertThat(foodList.get(0).getName()).isEqualTo("パン");

        List<Product> allList = inventoryService.getInventoryByCategory(null);
        assertThat(allList).hasSize(2);
    }

    // =========================================================================
    // UT-INV-004: アラート対象抽出
    // =========================================================================
    @Test
    @DisplayName("UT-INV-004: 在庫切れ・残りわずか・期限切れ・期限間近のいずれかに該当する商品が抽出されること")
    void utInv004_getAlertInventory_ShouldReturnAlertProducts() {
        Product alertProduct = new Product();
        alertProduct.setName("卵");
        alertProduct.setFamily(testFamily);
        alertProduct.setStock(0);
        alertProduct.setThreshold(1);
        alertProduct.setCategory(testCategory1);
        alertProduct.setUnit(testUnit);
        alertProduct.setThresholdUnit(testUnit);
        alertProduct.setIcon(testIcon);
        alertProduct.setIsDeleted(false);
        alertProduct.setIsNotify(true);
        productRepository.save(alertProduct);

        List<Product> alerts = inventoryService.getAlertInventory();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getName()).isEqualTo("卵");
    }

    // =========================================================================
    // UT-INV-005: ホーム画面用集計
    // =========================================================================
    @Test
    @DisplayName("UT-INV-005: ホーム画面用の各ステータス件数が正しく集計されること")
    void utInv005_getHomeSummary_ShouldReturnCorrectCounts() {
        Product outOfStockProduct = new Product();
        outOfStockProduct.setName("お米");
        outOfStockProduct.setFamily(testFamily);
        outOfStockProduct.setStock(0);
        outOfStockProduct.setThreshold(1);
        outOfStockProduct.setCategory(testCategory1);
        outOfStockProduct.setUnit(testUnit);
        outOfStockProduct.setThresholdUnit(testUnit);
        outOfStockProduct.setIcon(testIcon);
        outOfStockProduct.setIsDeleted(false);
        outOfStockProduct.setIsNotify(true);
        productRepository.save(outOfStockProduct);

        Map<String, Long> summary = inventoryService.getHomeSummary();

        assertThat(summary.get("outOfStockCount")).isEqualTo(1L);
        assertThat(summary.get("lowStockCount")).isEqualTo(0L);
    }

    // =========================================================================
    // UT-INV-006: 複数ステータスが混在するホーム画面集計の検証
    // =========================================================================
    @Test
    @DisplayName("UT-INV-006: 期限切れや残りわずか等の商品が複数混在している場合に、ホーム画面の各件数が正確に集計されること")
    void utInv006_getHomeSummary_ShouldCountMultipleStatusesCorrectly() {
        LocalDate today = LocalDate.now();

        Product expiredProduct = new Product();
        expiredProduct.setName("期限切れ牛乳");
        expiredProduct.setFamily(testFamily);
        expiredProduct.setStock(5);
        expiredProduct.setThreshold(1);
        expiredProduct.setExpiryDate(today.minusDays(2));
        expiredProduct.setCategory(testCategory1);
        expiredProduct.setUnit(testUnit);
        expiredProduct.setThresholdUnit(testUnit);
        expiredProduct.setIcon(testIcon);
        expiredProduct.setIsDeleted(false);
        expiredProduct.setIsNotify(true);
        productRepository.save(expiredProduct);

        Product lowStockProduct = new Product();
        lowStockProduct.setName("残りわずか卵");
        lowStockProduct.setFamily(testFamily);
        lowStockProduct.setStock(1);
        lowStockProduct.setThreshold(2);
        lowStockProduct.setExpiryDate(today.plusDays(10));
        lowStockProduct.setCategory(testCategory1);
        lowStockProduct.setUnit(testUnit);
        lowStockProduct.setThresholdUnit(testUnit);
        lowStockProduct.setIcon(testIcon);
        lowStockProduct.setIsDeleted(false);
        lowStockProduct.setIsNotify(true);
        productRepository.save(lowStockProduct);

        Map<String, Long> summary = inventoryService.getHomeSummary();

        assertThat(summary.get("expiredCount")).isEqualTo(1L);
        assertThat(summary.get("lowStockCount")).isEqualTo(1L);
        assertThat(summary.get("outOfStockCount")).isEqualTo(0L);
    }

    // =========================================================================
    // UT-INV-007: 登録データが0件の場合の空リスト返却検証
    // =========================================================================
    @Test
    @DisplayName("UT-INV-007: 在庫データが一件も登録されていない場合、各取得メソッドが空のリストを返すこと")
    void utInv007_emptyInventory_ShouldReturnEmptyLists() {
        assertThat(inventoryService.getAllInventory()).isEmpty();
        assertThat(inventoryService.searchInventory("テスト")).isEmpty();
        assertThat(inventoryService.getInventoryByCategory(testCategory1.getId())).isEmpty();
        assertThat(inventoryService.getAlertInventory()).isEmpty();
    }
}