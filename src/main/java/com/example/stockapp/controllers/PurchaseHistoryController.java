/*
 * 【役割】購入履歴画面の表示リクエストを受け取ります。
 * 過去の買い物記録をデータベースから取得して画面に渡す、参照専用のコントローラーです。
 *
 * TODO: クラスに @Controller と @RequiredArgsConstructor を付与する
 * TODO: @GetMapping("/purchase/history") を作成し、履歴一覧を取得してModelにセットする
 */
package com.example.stockapp.controllers;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.stockapp.entities.PurchaseHistory;
import com.example.stockapp.repositories.PurchaseHistoryRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // finalがついたフィールドを引数に持つコンストラクタをLombokで自動生成
@RequestMapping("/purchase")
public class PurchaseHistoryController {

    // 1. リポジトリをフィールドに定義（データベース操作用）
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    @GetMapping("/history")
    public ModelAndView history(ModelAndView mav) {
        // ※実際にはログイン中のユーザーの家族IDを取得しますが、
        // ここでは動作確認用として固定値（例: 1L）を設定しています。
        Long familyId = 1L;

        // 2. データベースから指定した家族の購入履歴を取得（日付の新しい順）
        List<PurchaseHistory> historyList = purchaseHistoryRepository.findByFamily_IdOrderByRecordDateDescIdDesc(familyId);

        // 3. 取得したリストを「購入日（recordDate）」ごとにグループ化する
        // （画面側の表示順を維持するため LinkedHashMap を使用）
        Map<LocalDate, List<PurchaseHistory>> historiesByDate = historyList.stream()
                .collect(Collectors.groupingBy(
                        PurchaseHistory::getRecordDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 4. HTMLに渡すデータをセット
        mav.addObject("historiesByDate", historiesByDate);

        // 5. 表示するHTMLファイル名を指定
        mav.setViewName("purchase/history");

        return mav;
    }
}
