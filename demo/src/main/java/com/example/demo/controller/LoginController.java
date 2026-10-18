package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// レッスン19：ログイン画面を表示するだけ（認証の処理は Spring Security が行う）
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
