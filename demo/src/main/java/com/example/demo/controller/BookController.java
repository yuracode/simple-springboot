package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.model.Book;
import com.example.demo.service.BookService;

import lombok.RequiredArgsConstructor;

// レッスン12〜13：書籍一覧・詳細
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping("/books")
    public String list(Model model) {
        model.addAttribute("books", service.findAll());
        return "books";
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable int id, Model model) {
        Book book = service.findById(id).orElse(null);
        if (book == null) {
            return "redirect:/books";   // 見つからなければ一覧へ戻す
        }
        model.addAttribute("book", book);
        return "book-detail";
    }
}
