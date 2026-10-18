package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exception.BookNotFoundException;
import com.example.demo.model.Book;
import com.example.demo.model.BookForm;
import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

// レッスン13：サービス層（コントローラとリポジトリの間）
// レッスン14：検索の判断をサービスに集める
// レッスン16：JPA 版のリポジトリに切り替える
// レッスン17〜18：登録・例外
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;

    public List<Book> findAll() {
        return repository.findAll(Sort.by("id"));
    }

    // キーワードが空なら全件、あればタイトルか著者で絞り込む
    public List<Book> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String k = keyword.trim();
        return repository.findByTitleContainingOrAuthorContainingOrderById(k, k);
    }

    // 見つからなければ BookNotFoundException
    public Book getById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book register(BookForm form) {
        Book book = new Book(null, form.getTitle(), form.getAuthor(), form.getPrice());
        return repository.save(book);
    }
}
