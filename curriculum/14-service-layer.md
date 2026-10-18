# レッスン14：サービス層に処理を集める（Controller → Service → Repository）

## 目標
書籍一覧に **キーワード検索**（`/books?keyword=Java`）を付ける。その際、「キーワードが空なら全件」という **判断** をサービスに書く。

## 考え方
| 層 | 担当すること | 書かないこと |
|---|---|---|
| コントローラ | リクエストを受け取り、ビューを選ぶ | SQL、業務のルール |
| サービス | 業務のルール（「空なら全件」など） | HTTP、SQL |
| リポジトリ | SQL を実行する | 画面、業務のルール |

コントローラに `if` や絞り込みを書くと、同じ処理を別の画面で使いたいときにコピペするしかなくなる。

## ステップ1：リポジトリに検索 SQL を追加（`repository/BookRepository.java`）
```java
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Book;

import lombok.RequiredArgsConstructor;

// レッスン12〜14：DB アクセス（教科書の DAO に相当）
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

    // レッスン14：タイトルか著者にキーワードを含む本
    public List<Book> findByKeyword(String keyword) {
        String sql = "SELECT id, title, author, price FROM books"
                + " WHERE title LIKE ? OR author LIKE ? ORDER BY id";
        String pattern = "%" + keyword + "%";
        return jdbcTemplate.query(sql, bookMapper, pattern, pattern);
    }
}
```
`%` は SQL 文字列ではなく、`?` に入れる **値の側** に付ける。

## ステップ2：サービスに判断を書く（`service/BookService.java`）
```java
package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

// レッスン13：サービス層（コントローラとリポジトリの間）
// レッスン14：検索の判断をサービスに集める
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;

    public List<Book> findAll() {
        return repository.findAll();
    }

    // キーワードが空なら全件、あればタイトルか著者で絞り込む
    public List<Book> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return repository.findByKeyword(keyword.trim());
    }

    public Optional<Book> findById(int id) {
        return repository.findById(id);
    }
}
```

## ステップ3：コントローラは受け取って渡すだけ（`controller/BookController.java`）
```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Book;
import com.example.demo.service.BookService;

import lombok.RequiredArgsConstructor;

// レッスン12〜13：書籍一覧・詳細
// レッスン14：キーワード検索
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("books", service.search(keyword));
        model.addAttribute("keyword", keyword);   // 検索欄に入力値を残すため
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
```
`@RequestParam(required = false)`：パラメータがなくてもエラーにしない（そのときは `null`）。

## ステップ4：検索フォームを付ける（`templates/books.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"><title>書籍一覧</title>
  <style>table { border-collapse: collapse; } th, td { border: 1px solid #999; padding: 4px 8px; }</style>
</head>
<body>
  <h1>書籍一覧（Thymeleaf 版）</h1>

  <!-- レッスン14：キーワード検索 -->
  <form action="/books" method="get">
    <input type="text" name="keyword" th:value="${keyword}" placeholder="タイトル・著者">
    <button type="submit">検索</button>
    <a href="/books">クリア</a>
  </form>

  <p th:if="${books.isEmpty()}">書籍が登録されていません。</p>
  <table th:unless="${books.isEmpty()}">
    <tr><th>ID</th><th>タイトル</th><th>著者</th><th>価格</th></tr>
    <tr th:each="b : ${books}">
      <td th:text="${b.id}"></td>
      <td><a th:href="@{/books/{id}(id=${b.id})}" th:text="${b.title}"></a></td>
      <td th:text="${b.author}"></td>
      <td th:text="|${#numbers.formatInteger(b.price, 1, 'COMMA')}円|"></td>
    </tr>
  </table>
  <p th:text="|全 ${books.size()} 冊|"></p>
  <a href="/">トップへ</a>
</body>
</html>
```

## 動作確認
- <http://localhost:8080/books?keyword=スッキリ> → 3 冊
- <http://localhost:8080/books?keyword=中山> → 著者で 2 冊
- キーワードを空で検索 → 全件

## 練習問題
サービスに `averagePrice()`（平均価格）を作り、一覧の下に表示する。計算はコントローラではなくサービスに書くこと。
