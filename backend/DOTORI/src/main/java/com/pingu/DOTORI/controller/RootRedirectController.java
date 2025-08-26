package com.pingu.DOTORI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    // ✅ 백엔드 루트로 오면 항상 프론트로 돌려보냄
    @GetMapping({"/", "/favicon.ico"})
    public String redirectToFront() {
        return "redirect:http://localhost:5173/";
    }
}
