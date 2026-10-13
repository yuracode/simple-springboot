# レッスン13：サービス層を挟む + 書籍の詳細ページ

## 目標
- コントローラ → **サービス** → リポジトリ の3層構成にする。
- 一覧のタイトルをクリックすると、その本の詳細ページ（`/books/{id}`）が開くようにする。

## 考え方
| 層 | 責任 | 教科書でいうと |
|---|---|---|
| コントローラ | リクエストを受け取り、ビューを選ぶ | サーブレット |
| サービス | 業務処理（計算・チェック・複数の DAO の組み合わせ） | Logic クラス |
| リポジトリ | SQL を実行する | DAO |

今のサービスはリポジトリを呼ぶだけ。それでも層を分けておくと、処理が増えたときにコントローラが肥大化しない。

## ステップ1：リポジトリに `findById` を追加（`repository/BookRepository.java`）
1行を `Book` に変換する処理を `findAll` と共通にしたいので、`RowMapper` としてフィールドに切り出す。
```java
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
```
- `WHERE id = ?` の `?` には、`query` の3つ目以降の引数（`id`）が入る（`PreparedStatement` の `setInt` に相当）。
- 見つからないこともあるので、戻り値は `Optional<Book>`（「あるかもしれないし、ないかもしれない」を表す型）にする。

## ステップ2：サービス（`service/BookService.java`）
```java
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
```

## ステップ3：コントローラ（`controller/BookController.java`）
`BookRepository` の代わりに `BookService` を使い、詳細ページ用のメソッドを追加する。
```java
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
```
`@PathVariable` はレッスン03で学んだもの。`/books/2` なら `id` に `2` が入る。

## ステップ4：一覧のタイトルをリンクにする（`templates/books.html`）
タイトルの `<td>` を次のように書き換える。
```html
<td><a th:href="@{/books/{id}(id=${b.id})}" th:text="${b.title}"></a></td>
```
`@{/books/{id}(id=${b.id})}` は、URL の `{id}` 部分に `b.id` を埋め込む Thymeleaf の書き方（→ `/books/2`）。

## ステップ5：詳細ページ（`templates/book-detail.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>書籍の詳細</title></head>
<body>
  <h1 th:text="${book.title}">タイトル</h1>
  <dl>
    <dt>ID</dt><dd th:text="${book.id}"></dd>
    <dt>著者</dt><dd th:text="${book.author}"></dd>
    <dt>価格</dt><dd th:text="|${#numbers.formatInteger(book.price, 1, 'COMMA')}円|"></dd>
  </dl>
  <a href="/books">一覧へ戻る</a>
</body>
</html>
```

## 動作確認
- <http://localhost:8080/books> でタイトルをクリックすると、詳細ページが開く
- <http://localhost:8080/books/2> を直接開いても表示される
- <http://localhost:8080/books/999>（存在しない ID）を開くと、一覧に戻される

## チェックリスト
- [ ] コントローラは `BookService` だけを使い、SQL を書いていない
- [ ] SQL に値を入れるときは `?` を使っている（文字列連結していない）
- [ ] 存在しない ID でもエラー画面にならない

## チャレンジ
- 書籍の登録フォームを作る（`@ModelAttribute` + `jdbcTemplate.update("INSERT ...")` + `redirect:/books`）。
- 詳細ページに「削除」ボタンを付ける（`@PostMapping("/books/{id}/delete")` + `DELETE` 文）。
