package com.bubble.giju.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/toss")
public class pageController {
    @GetMapping("/page")
    public String paymentPage() {
        return "payment";  // payment.html 렌더링
    }

}
