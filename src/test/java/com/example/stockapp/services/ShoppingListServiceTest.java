package com.example.stockapp.services;

import java.util.List;

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
import com.example.stockapp.entities.ShoppingListItem;
import com.example.stockapp.entities.Unit;
import com.example.stockapp.repositories.CategoryRepository;
import com.example.stockapp.repositories.FamilyRepository;
import com.example.stockapp.repositories.IconRepository;
import com.example.stockapp.repositories.ProductRepository;
import com.example.stockapp.repositories.ShoppingListItemRepository;
import com.example.stockapp.repositories.UnitRepository;

@SpringBootTest
@Transactional // テストごとにDBの変更を自動ロールバックする
class ShoppingListServiceTest {

    @Autowired
    private ShoppingListService shoppingListService;
    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;
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

    // 各テストで共通利用するマスタデータ
    private Family testFamily;
    private Category testCategory;
    private Unit testUnit;
    private Icon testIcon;

    @BeforeEach
    void setUp() {
        // 既存のテストデータが干渉しないようクリア
        shoppingListItemRepository.deleteAll();

        // Product保存時に外部キー制約で必須となる親マスタを準備
        testFamily = new Family();
        testFamily = familyRepository.findById(1L).orElseGet(() -> {
            Family newFamily = new Family();
            return familyRepository.save(newFamily);
        });

        testCategory = new Category();
        testCategory.setName("食品");
        testCategory = categoryRepository.save(testCategory);

        testUnit = new Unit();
        testUnit.setName("本");
        testUnit = unitRepository.save(testUnit);

        testIcon = new Icon();
        testIcon.setName("1.png");
        testIcon = iconRepository.save(testIcon);
    }

    // テスト用のProductを手軽に作るヘルパーメソッド
    private Product createSampleProduct(String name, int stock, int threshold) {
        Product product = new Product();
        product.setFamily(testFamily);
        product.setCategory(testCategory);
        product.setUnit(testUnit);
        product.setThresholdUnit(testUnit);
        product.setIcon(testIcon);
        product.setName(name);
        product.setStock(stock);
        product.setThreshold(threshold);
        product.setIsDeleted(false);
        return productRepository.save(product);
    }

    // =========================================================================
    // UT-SHP-001: 買い物リスト一覧表示（正常系 / データあり）
    // =========================================================================
    @Test
    @DisplayName("UT-SHP-001: 買い物リストにデータがある場合、全件取得できること")
    void utShp001_getShoppingList_WhenDataExists_ShouldReturnList() {
        // ① 準備 (Given): 商品と買い物リストアイテムを2件登録
        Product milk = createSampleProduct("牛乳", 2, 1);
        Product bread = createSampleProduct("食パン", 1, 1);

        ShoppingListItem item1 = new ShoppingListItem();
        item1.setFamily(testFamily);
        item1.setProduct(milk);
        item1.setThreshold(2);
        item1.setIsChecked(false);
        shoppingListItemRepository.save(item1);

        ShoppingListItem item2 = new ShoppingListItem();
        item2.setFamily(testFamily);
        item2.setProduct(bread);
        item2.setThreshold(1);
        item2.setIsChecked(true);
        shoppingListItemRepository.save(item2);

        // ② 実行 (When): 一覧取得メソッドを呼び出す
        // ※Service側のメソッド名（getShoppingList 等）に合わせて調整してください
        List<ShoppingListItem> actualList = shoppingListService.getShoppingList();

        // ③ 検証 (Then): 2件取得でき、中身が正しいことを確認
        assertThat(actualList).isNotNull();
        assertThat(actualList).hasSize(2);
        assertThat(actualList)
                .extracting(item -> item.getProduct().getName())
                .containsExactlyInAnyOrder("牛乳", "食パン");
    }

    // =========================================================================
    // UT-SHP-002: 在庫不足商品表示（業務ルール / threshold未満・以下）
    // =========================================================================
    @Test
    @DisplayName("UT-SHP-002: 在庫不足（または期限切れ）の商品が買い物リストに自動追加されること")
    void utShp002_autoAdd_WhenStockIsBelowThreshold_ShouldAppearInList() {
        // ① 準備 (Given)
        // 在庫切れ（stock = 0）、通知ONで作成
        Product lowStockProduct = createSampleProduct("醤油", 0, 2);
        lowStockProduct.setIsNotify(true);
        productRepository.save(lowStockProduct);

        // 在庫十分（stock = 5）、通知ONで作成（対象外になるべき）
        Product enoughStockProduct = createSampleProduct("みりん", 5, 2);
        enoughStockProduct.setIsNotify(true);
        productRepository.save(enoughStockProduct);

        // ② 実行 (When): それぞれ自動追加メソッドを呼ぶ
        shoppingListService.addAutoItem(lowStockProduct);
        shoppingListService.addAutoItem(enoughStockProduct);

        List<ShoppingListItem> actualList = shoppingListService.getShoppingList();

        // ③ 検証 (Then): 条件に合う「醤油」の1件だけがリストに入っていること
        assertThat(actualList).hasSize(1);
        assertThat(actualList.get(0).getProduct().getName()).isEqualTo("醤油");
    }

    // =========================================================================
    // UT-SHP-003: データなし（境界値 / 対象0件）
    // =========================================================================
    @Test
    @DisplayName("UT-SHP-003: 買い物リストの対象データが0件の場合、空のリストが返ること")
    void utShp003_getShoppingList_WhenNoData_ShouldReturnEmptyList() {
        // ① 準備 (Given): 買い物リストには何も登録しない（setUpで空の状態）

        // ② 実行 (When): 一覧取得メソッドを実行
        List<ShoppingListItem> actualList = shoppingListService.getShoppingList();

        // ③ 検証 (Then): nullではなく、要素数0の空リストが返ること
        assertThat(actualList).isNotNull();
        assertThat(actualList).isEmpty();
    }

    // =========================================================================
    // UT-SHP-004: 手動追加（未登録なら新規作成、登録済みなら数量加算）
    // =========================================================================
    @Test
    @DisplayName("UT-SHP-004: 商品を手動追加した際、未登録なら新規作成され、登録済みなら購入予定数量が加算されること")
    void utShp004_addManualItem_ShouldCreateOrAccumulate() {
        // ① 準備 (Given): テスト用の商品を作成
        Product product = createSampleProduct("マスク", 10, 5);

        // ② 実行 (When - 1回目): 未登録状態から手動追加（数量: 2）
        shoppingListService.addManualItem(product.getId(), 2);

        // ③ 検証 (Then - 1回目): 新規に1件登録され、数量が 2 になっていること
        List<ShoppingListItem> list = shoppingListService.getShoppingList();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getThreshold()).isEqualTo(2);

        // ② 実行 (When - 2回目): 同じ商品を再度手動追加（数量: 3）
        shoppingListService.addManualItem(product.getId(), 3);

        // ③ 検証 (Then - 2回目): リストの件数は1件のまま、数量が合計 5 (2 + 3) に加算されていること
        List<ShoppingListItem> updatedList = shoppingListService.getShoppingList();
        assertThat(updatedList).hasSize(1);
        assertThat(updatedList.get(0).getThreshold()).isEqualTo(5);
    }

    // =========================================================================
    // UT-SHP-005: チェック済み商品の一括購入完了
    // =========================================================================
    @Test
    @DisplayName("UT-SHP-005: チェック済み商品の購入完了時、商品の在庫が増加し買い物リストから該当アイテムが削除されること")
    void utShp005_completePurchase_ShouldUpdateStockAndClearList() {
        // ① 準備 (Given): 初期在庫 2 の商品を作成し、チェックONの買い物リストアイテムを登録
        Product product = createSampleProduct("卵", 2, 1);

        ShoppingListItem item = new ShoppingListItem();
        item.setFamily(testFamily);
        item.setProduct(product);
        item.setThreshold(1); // 購入予定数量: 1
        item.setIsChecked(true); // チェックON
        shoppingListItemRepository.save(item);

        // ② 実行 (When): 一括購入処理を実行
        int completedCount = shoppingListService.completePurchase();

        // ③ 検証 (Then):
        // 1. 完了件数が 1件であること
        assertThat(completedCount).isEqualTo(1);
        // 2. 買い物リストから該当アイテムが削除され、空になっていること
        assertThat(shoppingListService.getShoppingList()).isEmpty();
        // 3. 商品の在庫数が元の 2 から 3 (2 + 1) に増加していること
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(3);
    }

    // =========================================================================
    // UT-SHP-006: 不正な数量指定時の例外検証（異常系）
    // =========================================================================
    @Test
    @DisplayName("UT-SHP-006: 手動追加の数量に 0 やマイナスを指定した場合、例外が発生すること")
    void utShp006_addManualItem_InvalidQuantity_ShouldThrowException() {
        Product product = createSampleProduct("ティッシュ", 5, 2);

        // 実行 & 検証 (Then): 数量 0 を指定すると IllegalArgumentException が飛ぶこと
        assertThatThrownBy(() -> shoppingListService.addManualItem(product.getId(), 0))
                .isInstanceOf(IllegalArgumentException.class);

        // 実行 & 検証 (Then): マイナスを指定しても例外が飛ぶこと
        assertThatThrownBy(() -> shoppingListService.addManualItem(product.getId(), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================================
    // UT-SHP-007: チェック済みがない状態での一括購入（対象0件）
    // =========================================================================
    @Test
    @DisplayName("UT-SHP-007: チェックが入ったアイテムが無い状態で一括購入を実行した場合、処理件数0でリストが維持されること")
    void utShp007_completePurchase_NoCheckedItems_ShouldDoNothing() {
        Product product = createSampleProduct("ラップ", 5, 2);

        // チェック OFF のアイテムを登録
        ShoppingListItem item = new ShoppingListItem();
        item.setFamily(testFamily);
        item.setProduct(product);
        item.setThreshold(1);
        item.setIsChecked(false); // チェックOFF
        shoppingListItemRepository.save(item);

        // ① 実行 (When)
        int completedCount = shoppingListService.completePurchase();

        // ② 検証 (Then): 完了件数が 0 で、リストのデータが消えずに残っていること
        assertThat(completedCount).isEqualTo(0);
        assertThat(shoppingListService.getShoppingList()).hasSize(1);
    }
}