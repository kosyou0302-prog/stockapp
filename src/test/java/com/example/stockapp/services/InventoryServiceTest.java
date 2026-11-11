package com.example.stockapp.services;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stockapp.entities.Product;
import com.example.stockapp.repositories.ProductRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private InventoryService inventoryService;

    //商品0件の場合
    @Test
    void getHomeSummary_商品0件の場合は全て0件() {

        when(productRepository
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L))
                .thenReturn(List.of());

        Map<String, Long> result =
                inventoryService.getHomeSummary();

        assertEquals(0L, result.get("outOfStockCount"));
        assertEquals(0L, result.get("lowStockCount"));
        assertEquals(0L, result.get("expiredCount"));
        assertEquals(0L, result.get("nearExpiryCount"));

        verify(productRepository, times(1))
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L);
    }

    //ホーム画面集計
    @Test
    void getHomeSummary_各件数を正しく集計できる() {

        Product p1 = new Product();
        Product p2 = new Product();
        Product p3 = new Product();
        Product p4 = new Product();

        when(productRepository
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L))
                .thenReturn(List.of(p1, p2, p3, p4));

        when(productService.isOutOfStock(p1))
                .thenReturn(true);

        when(productService.isLowStock(p2))
                .thenReturn(true);

        when(productService.isPastExpiry(p3))
                .thenReturn(true);

        when(productService.isNearExpiry(p4))
                .thenReturn(true);

        Map<String, Long> result =
                inventoryService.getHomeSummary();

        assertEquals(1L, result.get("outOfStockCount"));
        assertEquals(1L, result.get("lowStockCount"));
        assertEquals(1L, result.get("expiredCount"));
        assertEquals(1L, result.get("nearExpiryCount"));
    }

    //アラート対象なし
    @Test
    void getAlertInventory_アラート対象なし() {

        Product product = new Product();

        when(productRepository
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L))
                .thenReturn(List.of(product));

        when(productService.isOutOfStock(any(Product.class)))
                .thenReturn(false);

        when(productService.isLowStock(any(Product.class)))
                .thenReturn(false);

        when(productService.isPastExpiry(any(Product.class)))
                .thenReturn(false);

        when(productService.isNearExpiry(any(Product.class)))
                .thenReturn(false);

        List<Product> result =
                inventoryService.getAlertInventory();

        assertTrue(result.isEmpty());
    }

    //アラート対象あり
    @Test
    void getAlertInventory_アラート対象のみ取得できる() {

        Product product1 = new Product();
        Product product2 = new Product();

        when(productRepository
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L))
                .thenReturn(List.of(product1, product2));

        when(productService.isLowStock(product1))
                .thenReturn(true);

        when(productService.isLowStock(product2))
                .thenReturn(false);

        when(productService.isOutOfStock(any(Product.class)))
                .thenReturn(false);

        when(productService.isPastExpiry(any(Product.class)))
                .thenReturn(false);

        when(productService.isNearExpiry(any(Product.class)))
                .thenReturn(false);

        List<Product> result =
                inventoryService.getAlertInventory();

        assertEquals(1, result.size());
        assertSame(product1, result.get(0));
    }

    //キーワード未入力
    @Test
    void searchInventory_nullの場合は全件取得() {

        when(productRepository
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L))
                .thenReturn(List.of());

        inventoryService.searchInventory(null);

        verify(productRepository, times(1))
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L);
    }

    // カテゴリ未選択

    @Test
    void getInventoryByCategory_nullの場合は全件取得() {

        when(productRepository
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L))
                .thenReturn(List.of());

        inventoryService.getInventoryByCategory(null);

        verify(productRepository, times(1))
                .findByFamily_IdAndIsDeletedFalseOrderByNameAsc(1L);
    }
}