package com.example.stockapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockapp.repositories.FamilyRepository;
import com.example.stockapp.repositories.ProductRepository;
import com.example.stockapp.repositories.PurchaseHistoryRepository;
import com.example.stockapp.repositories.ShoppingListItemRepository;

@SpringBootTest
@Transactional // テスト完了後に自動ロールバックされ、DBが汚れない
class ShoppingListServiceTest {

    @Autowired
    private ShoppingListService shoppingListService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;
    @Autowired
    private PurchaseHistoryRepository purchaseHistoryRepository;
    @Autowired
    private FamilyRepository familyRepository;

    // ここにテストケース（@Test）を書いていく

}