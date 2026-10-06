package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.model.Person;

// レッスン06-07：Lombok モデルとフォームバインド
@Controller
public class PersonController {

    @GetMapping("/person")
    public String person(Model model) {
        Person p = new Person("太郎", 20);
        p.setAge(p.getAge() + 1);
        model.addAttribute("person", p);
        return "person";
    }

    @GetMapping("/person/form")
    public String form(Model model) {
        model.addAttribute("person", new Person());
        return "person-form";
    }

    @PostMapping("/person/form")
    public String submit(@ModelAttribute Person person) {
        return "person-result";
    }
}
