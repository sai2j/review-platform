package com.nit;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String openLoginPage() {
        return "redirect:/login.html";
    }
}