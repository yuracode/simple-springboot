package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.model.Person;

// レッスン08-09：同じデータを Thymeleaf と JSP で表示する
@Controller
public class ViewDemoController {

    private List<Person> samplePeople() {
        return List.of(new Person("太郎", 20), new Person("花子", 17), new Person("次郎", 30));
    }

    @GetMapping("/th-demo")
    public String thDemo(Model model) {
        model.addAttribute("title", "Thymeleaf デモ");
        model.addAttribute("score", 75);
        model.addAttribute("people", samplePeople());
        return "th-demo";
    }

    @GetMapping("/jsp/person")
    public String jspPerson(Model model) {
        model.addAttribute("person", new Person("太郎", 20));
        model.addAttribute("people", samplePeople());
        return "jsp/person";
    }
}
