package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

// レッスン13：サービス層（コントローラとリポジトリの間）
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;

    public List<Book> findAll() {
        return repository.findAll();
    }

    public Optional<Book> findById(int id) {
        return repository.findById(id);
    }
}
