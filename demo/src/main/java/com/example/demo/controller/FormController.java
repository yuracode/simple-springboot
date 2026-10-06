package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// レッスン04-05：フォーム、GET/POST、Model
@Controller
public class FormController {

    @GetMapping("/form")
    public String showForm() {
        return "form";
    }

    @PostMapping("/form")
    @ResponseBody
    public String submit(@RequestParam String name, @RequestParam int age) {
        return name + "さんは" + age + "歳です。";
    }

    @GetMapping("/search")
    @ResponseBody
    public String search(@RequestParam String keyword) {
        return "検索キーワード：" + keyword;
    }

    @PostMapping("/form2")
    public String submit2(@RequestParam String name, @RequestParam int age, Model model) {
        model.addAttribute("name", name);
        model.addAttribute("age", age);
        model.addAttribute("adult", age >= 18);
        return "result";
    }

    // POST 後にリダイレクト（PRG パターン）
    @PostMapping("/form3")
    public String submit3() {
        return "redirect:/hello";
    }
}
