package com.example.stockapp.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.example.stockapp.entities.PurchaseHistory;
import com.example.stockapp.entities.Unit;
import com.example.stockapp.repositories.CategoryRepository;
import com.example.stockapp.repositories.FamilyRepository;
import com.example.stockapp.repositories.IconRepository;
import com.example.stockapp.repositories.ProductRepository;
import com.example.stockapp.repositories.PurchaseHistoryRepository;
import com.example.stockapp.repositories.UnitRepository;

@SpringBootTest
@Transactional
class PurchaseHistoryServiceTest {

    @Autowired
    private PurchaseHistoryService purchaseHistoryService;
    @Autowired
    private PurchaseHistoryRepository purchaseHistoryRepository;
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
    private Product testProduct;

    @BeforeEach
    void setUp() {
        purchaseHistoryRepository.deleteAll();
        productRepository.deleteAll();

        // 外部キー制約を満たすマスターデータと商品の準備
        testFamily = new Family();
        testFamily = familyRepository.save(testFamily);

        Category category = new Category();
        category.setName("食品");
        category = categoryRepository.save(category);

        Unit unit = new Unit();
        unit.setName("パック");
        unit = unitRepository.save(unit);

        Icon icon = new Icon();
        icon.setName("default.png");
        icon = iconRepository.save(icon);

        testProduct = new Product();
        testProduct.setFamily(testFamily);
        testProduct.setName("豆腐");
        testProduct.setStock(3);
        testProduct.setThreshold(1);
        testProduct.setCategory(category);
        testProduct.setUnit(unit);
        testProduct.setThresholdUnit(unit);
        testProduct.setIcon(icon);
        testProduct.setIsDeleted(false);
        testProduct.setIsNotify(true);
        testProduct = productRepository.save(testProduct);
    }

    // =========================================================================
    // UT-HIS-001: 購入履歴の記録処理（recordPurchase）
    // =========================================================================
    @Test
    @DisplayName("UT-HIS-001: 購入時に recordPurchase を実行すると、商品名スナップショットや数量が正しく保存されること")
    void utHis001_recordPurchase_ShouldSaveHistoryCorrectly() {
        // ① 実行 (When): 2個購入したとして履歴記録を呼び出し
        purchaseHistoryService.recordPurchase(testProduct, 2);

        // ② 検証 (Then): 履歴が1件保存されており、スナップショット名や数量が一致していること
        List<PurchaseHistory> histories = purchaseHistoryService.getPurchaseHistoryList(testFamily.getId());
        assertThat(histories).hasSize(1);

        PurchaseHistory savedHistory = histories.get(0);
        assertThat(savedHistory.getName()).isEqualTo("豆腐");
        assertThat(savedHistory.getQuantity()).isEqualTo(2);
        assertThat(savedHistory.getFamily().getId()).isEqualTo(testFamily.getId());
        assertThat(savedHistory.getRecordDate()).isEqualTo(LocalDate.now());
    }

    // =========================================================================
    // UT-HIS-002: 家族ID指定による履歴一覧取得処理
    // =========================================================================
    @Test
    @DisplayName("UT-HIS-002: 指定した家族IDの購入履歴一覧が正しく取得できること")
    void utHis002_getPurchaseHistoryList_ShouldReturnHistoryList() {
        // ① 準備 (Given): あらかじめ履歴を2件登録
        purchaseHistoryService.recordPurchase(testProduct, 1);
        purchaseHistoryService.recordPurchase(testProduct, 3);

        // ② 実行 (When): 家族IDを指定して履歴一覧を取得
        List<PurchaseHistory> historyList = purchaseHistoryService.getPurchaseHistoryList(testFamily.getId());

        // ③ 検証 (Then): 登録した2件の履歴が取得できていること
        assertThat(historyList).hasSize(2);
    }

    // =========================================================================
    // UT-HIS-003: 日付別グルーピング処理の検証
    // =========================================================================
    @Test
    @DisplayName("UT-HIS-003: 購入履歴が日付ごとに正しくグルーピングされて取得できること")
    void utHis003_getPurchaseHistoryGroupedByDate_ShouldReturnGroupedMap() {
        // ① 準備 (Given)
        purchaseHistoryService.recordPurchase(testProduct, 2);

        // ② 実行 (When): 日付別グルーピングを取得
        Map<LocalDate, List<PurchaseHistory>> groupedMap = purchaseHistoryService.getPurchaseHistoryGroupedByDate(testFamily.getId());

        // ③ 検証 (Then): 本日の日付のキーが存在し、リストに履歴が含まれていること
        assertThat(groupedMap).containsKey(LocalDate.now());
        assertThat(groupedMap.get(LocalDate.now())).hasSize(1);
    }

    // =========================================================================
    // UT-HIS-004: 不正なパラメータ時の例外検証（異常系）
    // =========================================================================
    @Test
    @DisplayName("UT-HIS-004: 不正な商品や購入数量を指定した場合、適切な例外がスローされること")
    void utHis004_recordPurchase_InvalidParams_ShouldThrowException() {
        // 実行 & 検証 (Then): 商品が null の場合
        assertThatThrownBy(() -> purchaseHistoryService.recordPurchase(null, 1))
                .isInstanceOf(IllegalArgumentException.class);

        // 実行 & 検証 (Then): 数量が 0 以下の場合
        assertThatThrownBy(() -> purchaseHistoryService.recordPurchase(testProduct, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================================
    // UT-HIS-005: 購入日が未設定の場合の自動補完検証
    // =========================================================================
    @Test
    @DisplayName("UT-HIS-005: 保存時に購入日（recordDate）が未設定の場合、当日の日付が自動でセットされること")
    void utHis005_savePurchaseHistory_ShouldAutoSetRecordDateIfNull() {
        // ① 準備 (Given): あえて recordDate を null にした履歴エンティティを作成
        PurchaseHistory history = new PurchaseHistory();
        history.setFamily(testFamily);
        history.setProduct(testProduct);
        history.setName(testProduct.getName());
        history.setQuantity(1);
        history.setRecordDate(null); // 未設定

        // ② 実行 (When)
        PurchaseHistory saved = purchaseHistoryService.savePurchaseHistory(history);

        // ③ 検証 (Then): 本日の日付が自動設定されていること
        assertThat(saved.getRecordDate()).isEqualTo(LocalDate.now());
    }

    // =========================================================================
    // UT-HIS-006: 家族IDが null の場合の一覧取得例外検証（異常系）
    // =========================================================================
    @Test
    @DisplayName("UT-HIS-006: 家族IDに null を指定して履歴一覧を取得しようとした場合、例外がスローされること")
    void utHis006_getPurchaseHistoryList_NullFamilyId_ShouldThrowException() {
        // 実行 & 検証 (Then)
        assertThatThrownBy(() -> purchaseHistoryService.getPurchaseHistoryList(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}