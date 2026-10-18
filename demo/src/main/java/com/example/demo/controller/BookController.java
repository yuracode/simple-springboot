package com.example.demo.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Book;
import com.example.demo.model.BookForm;
import com.example.demo.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// レッスン12〜13：書籍一覧・詳細
// レッスン14：キーワード検索
// レッスン17〜18：登録フォーム + 入力チェック
// レッスン19：ログイン中のユーザー名を表示
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String keyword,
                       Principal principal, Model model) {
        model.addAttribute("books", service.search(keyword));
        model.addAttribute("keyword", keyword);
        // ログインしていなければ principal は null
        model.addAttribute("username", principal == null ? null : principal.getName());
        return "books";
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable int id, Model model) {
        Book book = service.getById(id);   // 見つからなければ例外 → GlobalExceptionHandler
        model.addAttribute("book", book);
        return "book-detail";
    }

    @GetMapping("/books/new")
    public String newForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        return "book-form";
    }

    @PostMapping("/books")
    public String create(@Valid @ModelAttribute BookForm bookForm, BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "book-form";   // エラーがあればフォームを再表示（入力値は残る）
        }
        Book saved = service.register(bookForm);
        redirectAttributes.addFlashAttribute("message", "「" + saved.getTitle() + "」を登録しました");
        return "redirect:/books";
    }
}
