/*
 * 【役割】アプリ起動時（URLのルート "/"）のアクセスを受け取り、最初のホーム画面を表示する窓口です。
 *
 */
package com.example.stockapp.controllers;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.stockapp.services.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    // ログイン機能ができるまでの暫定対応：ID=1のダミー家族をセット
    private static final Long FAMILY_ID = 1L;

    // ホーム画面------(URL: http://localhost:8080/)
    @GetMapping("/")
    public ModelAndView home(ModelAndView mav) {
        mav.setViewName("home");
        Map<String, Long> homeSummary = productService.getHomeSummary(FAMILY_ID);
        mav.addObject("homeSummary", homeSummary);
        return mav;
    }
}