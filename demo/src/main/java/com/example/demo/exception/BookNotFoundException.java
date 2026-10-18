package com.example.demo.exception;

// レッスン18：指定した ID の本がないときに投げる例外
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(int id) {
        super("ID " + id + " の書籍は見つかりませんでした");
    }
}
