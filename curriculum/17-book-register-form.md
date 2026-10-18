# レッスン17：Thymeleaf で登録フォームを作る（フォーム → 登録 → 一覧）

## 目標
`/books/new` のフォームで本を登録し、一覧に戻って「登録しました」と表示する。
```
GET  /books/new ─▶ フォーム表示
POST /books     ─▶ 登録 ─▶ redirect:/books
GET  /books     ─▶ 一覧（「○○を登録しました」）
```

## 考え方
- **フォーム用のクラス `BookForm` を分ける**：`Book` には `id` があるが、フォームで `id` を送らせたくない。
- **PRG パターン（Post → Redirect → Get）**：POST のあとにビューを直接返すと、F5 で同じ本がもう一度登録される。POST のあとは `redirect:` する。
- リダイレクトすると `Model` は消えるので、メッセージは **フラッシュ属性**（次の1回だけ残る値）で渡す。

## ステップ1：フォームクラス（`model/BookForm.java`）
```java
package com.example.demo.model;

import lombok.Data;

// レッスン17：登録フォームの入力値（エンティティの Book とは分ける）
@Data
public class BookForm {
    private String title;
    private String author;
    private Integer price;
}
```

## ステップ2：サービスに登録処理（`service/BookService.java`）
```java
package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Book;
import com.example.demo.model.BookForm;
import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

// レッスン13：サービス層（コントローラとリポジトリの間）
// レッスン14：検索の判断をサービスに集める
// レッスン16：JPA 版のリポジトリに切り替える
// レッスン17：登録
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;

    public List<Book> findAll() {
        return repository.findAll(Sort.by("id"));
    }

    public List<Book> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String k = keyword.trim();
        return repository.findByTitleContainingOrAuthorContainingOrderById(k, k);
    }

    public Optional<Book> findById(int id) {
        return repository.findById(id);
    }

    // フォーム → エンティティに詰め替えて保存する
    @Transactional
    public Book register(BookForm form) {
        Book book = new Book(null, form.getTitle(), form.getAuthor(), form.getPrice());
        return repository.save(book);   // id が null なので INSERT。DB が振った id 入りで返る
    }
}
```
`@Transactional`：途中で例外が起きたら DB の変更を取り消す。DB を書き換えるメソッドに付ける。

## ステップ3：コントローラ（`controller/BookController.java`）
```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Book;
import com.example.demo.model.BookForm;
import com.example.demo.service.BookService;

import lombok.RequiredArgsConstructor;

// レッスン12〜13：書籍一覧・詳細
// レッスン14：キーワード検索
// レッスン17：登録フォーム
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("books", service.search(keyword));
        model.addAttribute("keyword", keyword);
        return "books";
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable int id, Model model) {
        Book book = service.findById(id).orElse(null);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        return "book-detail";
    }

    // フォームを表示する（空の BookForm を渡す）
    @GetMapping("/books/new")
    public String newForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        return "book-form";
    }

    // 登録する → 一覧へリダイレクト（PRG パターン）
    @PostMapping("/books")
    public String create(@ModelAttribute BookForm bookForm, RedirectAttributes redirectAttributes) {
        Book saved = service.register(bookForm);
        redirectAttributes.addFlashAttribute("message", "「" + saved.getTitle() + "」を登録しました");
        return "redirect:/books";
    }
}
```
`/books/new` と `/books/{id}` は、固定の文字列（`new`）のほうが優先されるのでぶつからない。

## ステップ4：フォーム画面（`templates/book-form.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>書籍の登録</title></head>
<body>
  <h1>書籍の登録</h1>
  <form th:action="@{/books}" th:object="${bookForm}" method="post">
    タイトル：<input type="text" th:field="*{title}"><br>
    著者：<input type="text" th:field="*{author}"><br>
    価格：<input type="text" th:field="*{price}">円<br>
    <button type="submit">登録する</button>
  </form>
  <a href="/books">一覧へ戻る</a>
</body>
</html>
```
`th:field="*{title}"` は `name="title"` `id="title"` `value="…"` をまとめて出力する（レッスン07・10と同じ）。

## ステップ5：一覧にメッセージと登録リンク（`templates/books.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"><title>書籍一覧</title>
  <style>
    table { border-collapse: collapse; } th, td { border: 1px solid #999; padding: 4px 8px; }
    .message { color: green; }
  </style>
</head>
<body>
  <h1>書籍一覧（Thymeleaf 版）</h1>

  <!-- レッスン17：登録後のメッセージ（フラッシュ属性） -->
  <p class="message" th:if="${message}" th:text="${message}"></p>

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
  <p><a href="/books/new">新しい本を登録する</a></p>
  <a href="/">トップへ</a>
</body>
</html>
```

## 動作確認
1. <http://localhost:8080/books/new> で登録 → 一覧に「「○○」を登録しました」と新しい本が出る。
2. そのまま F5 → メッセージは消え、本は増えない（PRG パターン）。

> この時点では、タイトルが空でも登録できてしまい、価格に `abc` を入れると 400 エラーになる。→ レッスン18で直す。

## 練習問題
登録後、一覧ではなく登録した本の詳細ページにリダイレクトする（`"redirect:/books/" + saved.getId()`）。
