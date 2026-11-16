
/*
 * 【役割】アプリ起動時（URLのルート "/"）のアクセスを受け取り、最初のホーム画面を表示する窓口です。
 *
 */
package com.example.stockapp.controllers;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.stockapp.services.InventoryService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    // 【修正】ProductServiceからInventoryServiceに変更
    private final InventoryService inventoryService;

    // ホーム画面------(URL: http://localhost:8080/)
    @GetMapping("/")
    public ModelAndView home(ModelAndView mav) {
        mav.setViewName("home");

        // 【修正】通知ONの商品だけを集計するInventoryServiceを使用
        Map<String, Long> homeSummary = inventoryService.getHomeSummary();

        mav.addObject("homeSummary", homeSummary);
        return mav;
    }
}
