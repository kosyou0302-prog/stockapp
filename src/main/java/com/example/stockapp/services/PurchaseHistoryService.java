/*
 * 【役割】購入履歴の記録と、画面表示用のデータ取得ロジックを担当します。
 * 購入処理（ShoppingListService）の中から呼び出されて、履歴データの生成を行います。
 *
 * TODO: クラスに @Service と @Transactional を付与する
 * TODO: 新しい購入履歴を保存するメソッドを実装する
 * TODO: 画面表示用に購入履歴一覧を新しい順で取得するメソッドを実装する
 */
package com.example.stockapp.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockapp.entities.PurchaseHistory;
import com.example.stockapp.repositories.PurchaseHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseHistoryService {

    private final PurchaseHistoryRepository purchaseHistoryRepository;

    // 【処理内容】履歴の保存：「購入済みにする」操作が行われた際、対象商品のスナップショット（商品名・数量・購入日など）を保存する
    public PurchaseHistory savePurchaseHistory(PurchaseHistory purchaseHistory){

        if (purchaseHistory.getRecordDate() == null) {
            purchaseHistory.setRecordDate(LocalDate.now());
        }
        return purchaseHistoryRepository.save(purchaseHistory);
    }
    public List<PurchaseHistory>getPurchaseHistoryList(Long familyId){

         // 購入日が設定されていない場合は当日の日付をセットする
        return purchaseHistoryRepository.findByFamily_IdOrderByRecordDateDesc(familyId);
    }


    // 【処理内容】日付別グルーピング：取得した履歴データを、Javaの機能（Collectors.groupingByなど）を用いて日付ごとのMapに変換して返す
    public Map<LocalDate, List<PurchaseHistory>> getPurchaseHistoryGroupedByDate(Long familyId) {
        List<PurchaseHistory> historyList = getPurchaseHistoryList(familyId);
        
        // recordDate（購入日）ごとにグループ化する
        return historyList.stream()
                .collect(Collectors.groupingBy(PurchaseHistory::getRecordDate));
    }

}
