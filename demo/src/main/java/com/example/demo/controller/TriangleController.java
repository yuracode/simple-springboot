package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Triangle;
import com.example.demo.service.TriangleService;

import lombok.RequiredArgsConstructor;

// レッスン10：三角形の面積を求める
@Controller
@RequiredArgsConstructor
public class TriangleController {

    private final TriangleService service;

    // ステップ1：一番シンプルな版
    @GetMapping("/triangle/simple")
    public String simpleForm() {
        return "triangle-simple";
    }

    @PostMapping("/triangle/simple")
    public String simpleCalc(@RequestParam double base, @RequestParam double height, Model model) {
        model.addAttribute("base", base);
        model.addAttribute("height", height);
        model.addAttribute("area", base * height / 2);
        return "triangle-simple";
    }

    // ステップ4-5：モデル + サービス + 入力チェック（Thymeleaf）
    @GetMapping("/triangle")
    public String form(Model model) {
        model.addAttribute("triangle", new Triangle());
        return "triangle";
    }

    @PostMapping("/triangle")
    public String calc(@Validated @ModelAttribute Triangle triangle, BindingResult result) {
        if (!result.hasErrors()) {
            service.calcArea(triangle);
        }
        return "triangle";
    }

    // ステップ6：JSP 版
    @GetMapping("/jsp/triangle")
    public String jspForm(Model model) {
        model.addAttribute("triangle", new Triangle());
        return "jsp/triangle";
    }

    @PostMapping("/jsp/triangle")
    public String jspCalc(@Validated @ModelAttribute Triangle triangle, BindingResult result) {
        if (!result.hasErrors()) {
            service.calcArea(triangle);
        }
        return "jsp/triangle";
    }
}
