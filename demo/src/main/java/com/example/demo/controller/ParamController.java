package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// レッスン02-03：パラメータの受け取り
@Controller
public class ParamController {

    @GetMapping("/greet")
    @ResponseBody
    public String greet(@RequestParam String name) {
        return "こんにちは、" + name + "さん";
    }

    @GetMapping("/greet2")
    @ResponseBody
    public String greet2(@RequestParam(defaultValue = "ゲスト") String name) {
        return "こんにちは、" + name + "さん";
    }

    @GetMapping("/add")
    @ResponseBody
    public String add(@RequestParam int a, @RequestParam int b) {
        return a + " + " + b + " = " + (a + b);
    }

    @GetMapping("/users/{id}")
    @ResponseBody
    public String user(@PathVariable int id) {
        return "ユーザーID = " + id;
    }

    @GetMapping("/square/{n}")
    @ResponseBody
    public String square(@PathVariable int n) {
        return n + " の2乗 = " + (n * n);
    }
}
