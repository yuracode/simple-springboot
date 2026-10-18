package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

// レッスン18：すべてのコントローラに共通の例外処理
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)   // ステータスコード 404 で返す
    public String handleBookNotFound(BookNotFoundException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "book-not-found";
    }
}
