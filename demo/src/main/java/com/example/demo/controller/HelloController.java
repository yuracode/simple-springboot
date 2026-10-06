package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// レッスン01：一番シンプルなコントローラ（パラメータなし）
@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("now", LocalDateTime.now());
        return "hello";
    }

    @GetMapping("/hello-text")
    @ResponseBody
    public String helloText() {
        return "テキストでこんにちは";
    }
}
