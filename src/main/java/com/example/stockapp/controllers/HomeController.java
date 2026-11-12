/*
 * 【役割】アプリ起動時（URLのルート "/"）のアクセスを受け取り、最初のホーム画面を表示する窓口です。
 *
 * TODO: クラスに @Controller と @RequiredArgsConstructor を付与する
 * TODO: @GetMapping("/") を作成し、Serviceから集計データ（アラート件数など）を受け取る
 * TODO: 受け取ったデータを Model にセットし、"home"（home.html）を返す
 */
package com.example.stockapp.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.stockapp.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductRepository productRepository;

    // ログイン機能ができるまでの暫定対応：ID=1のダミー家族をセット
    private static final Long FAMILY_ID = 1L;

    // ホーム画面------(URL: http://localhost:8080/)
    @GetMapping("/")
    public ModelAndView home(ModelAndView mav) {
        mav.setViewName("home");

        // 今日分の日付（期限切れ・期限間近の集計用）を取得
        LocalDate today = LocalDate.now();

        // 在庫切れ件数
        long outOfStockCount =
            productRepository.countByFamily_IdAndStockAndIsDeletedFalse(
                    FAMILY_ID, 0);

        // 期限切れ件数
        long expiredCount =
            productRepository.countByFamily_IdAndExpiryDateBeforeAndIsDeletedFalse(
                    FAMILY_ID, today);

        // 残りわずか件数
        long lowStockCount =
            productRepository.countLowStock(FAMILY_ID);

        // 期限間近件数
        long expiringSoonCount =
            productRepository.countExpiringSoon(
                        FAMILY_ID, today);

        Map<String, Long> homeSummary = new HashMap<>();

        homeSummary.put("outOfStockCount", outOfStockCount);
        homeSummary.put("expiredCount", expiredCount);
        homeSummary.put("lowStockCount", lowStockCount);
        homeSummary.put("expiringSoonCount", expiringSoonCount);

        mav.addObject("homeSummary", homeSummary);

        return mav;
    }
}