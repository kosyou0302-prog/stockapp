/*
 * 【役割】購入履歴の記録と、画面表示用のデータ取得ロジックを担当します。
 * 購入処理（ShoppingListService）の中から呼び出されて、履歴データの生成を行います。
 */
package com.example.stockapp.services;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockapp.entities.Product;
import com.example.stockapp.entities.PurchaseHistory;
import com.example.stockapp.repositories.PurchaseHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseHistoryService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;

    /**
     * 【履歴保存】購入履歴エンティティを永続化します。
     * 購入日（recordDate）が未設定の場合は、当日の日付を自動セットします。
     */
    public PurchaseHistory savePurchaseHistory(PurchaseHistory purchaseHistory){
        if (purchaseHistory == null) {
            throw new IllegalArgumentException("購入履歴データが指定されていません");
        }
        if (purchaseHistory.getRecordDate() == null) {
            purchaseHistory.setRecordDate(LocalDate.now());
        }
        return purchaseHistoryRepository.save(purchaseHistory);
    }

    /**
     * 【一覧取得】指定した家族の購入履歴一覧を、購入日の新しい順（降順）で取得します。
     *
     * @param familyId 家族ID
     * @return 購入日の降順で整列された購入履歴リスト
     */
    @Transactional(readOnly = true)
    public List<PurchaseHistory>getPurchaseHistoryList(Long familyId){
        if (familyId == null) {
            throw new IllegalArgumentException("家族IDを指定してください");
        }
        return purchaseHistoryRepository.findByFamily_IdOrderByRecordDateDescIdDesc(familyId);
    }

    /**
     * 【日付別グルーピング】購入履歴を購入日（降順の順序を維持）ごとにグループ化して返します。
     * 画面側で「今日買ったもの」「昨日買ったもの」のセクション分け表示に使用します。
     *
     * @param familyId 家族ID
     * @return 購入日の降順を保持した、日付ごとの購入履歴Map
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, List<PurchaseHistory>> getPurchaseHistoryGroupedByDate(Long familyId) {
        List<PurchaseHistory> historyList = getPurchaseHistoryList(familyId);

        // LinkedHashMap を指定し、リポジトリのソート順（新しい日付順）を崩さずに保持
        return historyList.stream()
                .collect(Collectors.groupingBy(
                    PurchaseHistory::getRecordDate,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
    }

    /**
     * 【一括購入連携】購入完了時に商品情報と購入数量を受け取り、履歴を記録します。
     * 商品マスタが将来変更・削除されても購入当時の状態が残るようスナップショットを生成します。
     *
     * @param product 購入対象の商品
     * @param quantity 今回購入した数量
     * @return 永続化された購入履歴エンティティ
     */
    public PurchaseHistory recordPurchase(Product product, Integer quantity) {
        if (product == null) {
            throw new IllegalArgumentException("商品が指定されていません");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("購入数量は1以上で指定してください");
        }
        PurchaseHistory history = new PurchaseHistory();
        history.setFamily(product.getFamily());
        history.setProduct(product);
        history.setName(product.getName());
        history.setQuantity(quantity);
        history.setRecordDate(LocalDate.now());
        return savePurchaseHistory(history);
    }
}
