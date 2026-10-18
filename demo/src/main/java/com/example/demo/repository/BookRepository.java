package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Book;

// レッスン16：Spring Data JPA 版のリポジトリ（インターフェースを書くだけで動く）
public interface BookRepository extends JpaRepository<Book, Integer> {

    // メソッド名から SQL が自動で作られる（派生クエリ）
    // → WHERE title LIKE '%…%' OR author LIKE '%…%' ORDER BY id
    List<Book> findByTitleContainingOrAuthorContainingOrderById(String title, String author);
}
