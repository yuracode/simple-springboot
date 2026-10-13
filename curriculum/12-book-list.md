# レッスン12：書籍一覧を表示する（JdbcTemplate）

## 目標
`books` テーブルの全件を取り出し、`/books` で表にして表示する。

## 設計
| 層 | ファイル | 役割 |
|---|---|---|
| モデル | `model/Book.java` | テーブルの1行 |
| リポジトリ | `repository/BookRepository.java` | SQL を実行する（教科書の **DAO** に相当） |
| コントローラ | `controller/BookController.java` | 一覧を取り出してビューに渡す |
| ビュー | `templates/books.html` | 表で表示する |

## ステップ1：モデル（`model/Book.java`）
```java
package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// レッスン12：書籍（books テーブルの1行に対応）
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Integer id;
    private String title;
    private String author;
    private Integer price;
}
```

## ステップ2：リポジトリ（`repository/BookRepository.java`）
```java
package com.example.demo.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Book;

import lombok.RequiredArgsConstructor;

// レッスン12：DB アクセス（教科書の DAO に相当）
@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Book> findAll() {
        String sql = "SELECT id, title, author, price FROM books ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("price")));
    }
}
```
### DAO との比較
| サーブレット/JSP の DAO | Spring の JdbcTemplate |
|---|---|
| `Class.forName(...)` / `DriverManager.getConnection(...)` | 不要（`application.properties` の設定から自動で接続） |
| `PreparedStatement` / `ResultSet` / `close()` | 不要（`jdbcTemplate.query` がすべて行う） |
| `while (rs.next()) { ... list.add(...) }` | 1行分の変換だけをラムダ `(rs, rowNum) -> ...` で書く |
| `SQLException` の try-catch | 不要（実行時例外に変換される） |

`JdbcTemplate` は Spring が用意している部品なので、`@RequiredArgsConstructor` + `private final` で **DI（注入）** してもらうだけで使える。

## ステップ3：コントローラ（`controller/BookController.java`）
まずはリポジトリを直接使う（レッスン13でサービス層を挟む）。
```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookRepository repository;

    @GetMapping("/books")
    public String list(Model model) {
        model.addAttribute("books", repository.findAll());
        return "books";
    }
}
```

## ステップ4：ビュー（`templates/books.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"><title>書籍一覧</title>
  <style>table { border-collapse: collapse; } th, td { border: 1px solid #999; padding: 4px 8px; }</style>
</head>
<body>
  <h1>書籍一覧（Thymeleaf 版）</h1>
  <p th:if="${books.isEmpty()}">書籍が登録されていません。</p>
  <table th:unless="${books.isEmpty()}">
    <tr><th>ID</th><th>タイトル</th><th>著者</th><th>価格</th></tr>
    <tr th:each="b : ${books}">
      <td th:text="${b.id}"></td>
      <td th:text="${b.title}"></td>
      <td th:text="${b.author}"></td>
      <td th:text="|${#numbers.formatInteger(b.price, 1, 'COMMA')}円|"></td>
    </tr>
  </table>
  <p th:text="|全 ${books.size()} 冊|"></p>
  <a href="/">トップへ</a>
</body>
</html>
```
- `th:each="b : ${books}"`：リストの件数だけ `<tr>` を繰り返す
- `#numbers.formatInteger(..., 'COMMA')`：`2860` → `2,860`

## 動作確認
<http://localhost:8080/books> を開くと、4 冊が表で表示される。

## 練習問題
1. pgAdmin で `INSERT` して1冊増やし、ブラウザを再読み込みすると一覧に増えることを確かめる。
2. `ORDER BY price DESC` にして、値段の高い順に並べる。
3. `findByAuthor(String author)` を作り、`/books?author=中山清喬` で絞り込めるようにする。
   ヒント：`jdbcTemplate.query("... WHERE author = ?", rowMapper, author)`（文字列連結ではなく `?` を使うこと）
