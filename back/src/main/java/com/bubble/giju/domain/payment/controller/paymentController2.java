package com.bubble.giju.domain.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/payment")
public class paymentController2 {
    @GetMapping("/page")
    public String paymentPage() {
        return "payment";  // payment.html 렌더링
    }

}
