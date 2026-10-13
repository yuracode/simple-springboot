package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Book;

import lombok.RequiredArgsConstructor;

// レッスン12〜13：DB アクセス（教科書の DAO に相当）
@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    // 1行 → Book の変換（findAll と findById で共通）
    private final RowMapper<Book> bookMapper = (rs, rowNum) -> new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getInt("price"));

    public List<Book> findAll() {
        String sql = "SELECT id, title, author, price FROM books ORDER BY id";
        return jdbcTemplate.query(sql, bookMapper);
    }

    public Optional<Book> findById(int id) {
        String sql = "SELECT id, title, author, price FROM books WHERE id = ?";
        return jdbcTemplate.query(sql, bookMapper, id).stream().findFirst();
    }
}
