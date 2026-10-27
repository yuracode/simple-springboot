# 付録A：書籍のメンテナンス（登録・更新・削除）を JdbcTemplate で作る

## 目標
レッスン12〜14で作った書籍一覧に、**登録・編集・削除**を付ける。
SQL は今までと同じく `JdbcTemplate` で **自分で書く**。

```
GET  /books              ─▶ 一覧（「○○を登録しました」などのメッセージ）
GET  /books/new          ─▶ 登録フォーム
POST /books              ─▶ INSERT ─▶ redirect:/books
GET  /books/{id}/edit    ─▶ 編集フォーム
POST /books/{id}         ─▶ UPDATE ─▶ redirect:/books
POST /books/{id}/delete  ─▶ DELETE ─▶ redirect:/books
```

## 考え方
- データを「一覧・登録・更新・削除」する機能をまとめて **CRUD**（Create / Read / Update / Delete）と呼ぶ。SQL でいうと `INSERT` / `SELECT` / `UPDATE` / `DELETE`。
- 読み出し（`SELECT`）は `jdbcTemplate.query(...)` だった。書き込み（`INSERT` / `UPDATE` / `DELETE`）は **`jdbcTemplate.update(...)`** を使う。戻り値は「変更された行数」。
- **PRG パターン（Post → Redirect → Get）**：POST のあとにビューを直接返すと、F5 で同じ登録がもう一度送られる。POST のあとは必ず `redirect:` する。
- リダイレクトすると `Model` の中身は消えるので、「登録しました」のメッセージは **フラッシュ属性**（次の1回だけ残る値）で渡す。
- 更新と削除は HTML のフォームから送るので、PUT / DELETE ではなく **POST** を使う。

> **メモ：もっと楽な書き方もある**
> このレッスンでは、SQL も「1行 → `Book` への変換」もすべて手で書く。少し面倒だが、**データベースとのやり取りで実際に何が起きているか** がそのまま見える書き方である。
> Spring には、こうした決まりきった SQL を自動で作ってくれる仕組み（**O/R マッパー**）もあり、レッスン16の Spring Data JPA で登場する。今はまだ使わない。手で書いた経験があると、自動化されたときに「裏でこの SQL が動いているのか」と理解しやすくなる。

## 設計
| 層 | ファイル | このレッスンでやること |
|---|---|---|
| リポジトリ | `repository/BookRepository.java` | `insert` / `update` / `deleteById` を追加 |
| コントローラ | `controller/BookController.java` | フォーム表示と POST の受け口を追加（**リポジトリを直接使う**） |
| ビュー | `templates/book-form.html` | **新規**：登録と編集で共通のフォーム |
| ビュー | `templates/books.html` | 「新しく登録する」「編集」「削除」を追加 |

モデル（`model/Book.java`）はレッスン12のまま変えない。

> **メモ：この付録Aではサービス層を使わない**
> レッスン13〜14ではコントローラとリポジトリの間にサービスを挟んだが、この付録Aでは **コントローラから `BookRepository` を直接呼ぶ**（レッスン12と同じ形）。登録・更新・削除で SQL がどう動くかに集中するためである。
> 「サービスを挟んでおくと、リポジトリの作り方を変えてもコントローラを直さずに済む」という話は、**付録B**（[AppendixB-book-service-layer.md](AppendixB-book-service-layer.md)）で扱う。

## ステップ1：リポジトリ（`repository/BookRepository.java`）
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
// 付録A：登録・更新・削除
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

    // 付録A：登録（id は SERIAL なので DB が自動で振る）
    public void insert(Book book) {
        String sql = "INSERT INTO books (title, author, price) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, book.getTitle(), book.getAuthor(), book.getPrice());
    }

    // 付録A：更新（戻り値は更新された行数。0 ならその id は無い）
    public int update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, price = ? WHERE id = ?";
        return jdbcTemplate.update(sql, book.getTitle(), book.getAuthor(), book.getPrice(), book.getId());
    }

    // 付録A：削除（戻り値は削除された行数。0 ならその id は無い）
    public int deleteById(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
```
### ポイント
- `update(sql, 値1, 値2, ...)` の値は、SQL の `?` に **前から順番に** 入る。`UPDATE` では `WHERE id = ?` が最後なので、`book.getId()` も最後に渡す。
- `INSERT` の列に `id` を書かない。`SERIAL` なので DB が次の番号を振ってくれる。
- 教科書の DAO と比べると、`PreparedStatement` の作成・`setString`・`executeUpdate`・`close` がすべて `jdbcTemplate.update` の1行に入っている。

| サーブレット/JSP の DAO | JdbcTemplate |
|---|---|
| `pStmt.setString(1, title); ...` | `update(sql, title, ...)` の引数に並べる |
| `int result = pStmt.executeUpdate();` | `int result = jdbcTemplate.update(...);` |
| `if (result != 1) return false;` | 戻り値が 0 かどうかで判断する |

## ステップ2：コントローラ（`controller/BookController.java`）
```java
package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

// 付録A：書籍の一覧・詳細・登録・編集・削除（リポジトリを直接使う）
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookRepository repository;

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        // キーワードが空なら全件、あればタイトルか著者で絞り込む
        List<Book> books = (keyword == null || keyword.isBlank())
                ? repository.findAll()
                : repository.findByKeyword(keyword.trim());
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);   // 検索欄に入力値を残すため
        return "books";
    }

    // 「/books/new」を「/books/{id}」より先に書く必要はない（Spring は固定の URL を優先する）
    @GetMapping("/books/new")
    public String newForm(Model model) {
        model.addAttribute("book", new Book());   // 空のフォーム
        return "book-form";
    }

    @PostMapping("/books")
    public String create(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        repository.insert(book);
        redirectAttributes.addFlashAttribute("message", "「" + book.getTitle() + "」を登録しました");
        return "redirect:/books";
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable int id, Model model) {
        Book book = repository.findById(id).orElse(null);
        if (book == null) {
            return "redirect:/books";   // 見つからなければ一覧へ戻す
        }
        model.addAttribute("book", book);
        return "book-detail";
    }

    @GetMapping("/books/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Book book = repository.findById(id).orElse(null);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);         // 今の値を入れたフォーム
        return "book-form";
    }

    @PostMapping("/books/{id}")
    public String update(@PathVariable int id, @ModelAttribute Book book,
            RedirectAttributes redirectAttributes) {
        book.setId(id);   // どの本を更新するかは URL の id で決める
        // 更新された行数が 1 なら成功、0 ならその id は無い
        if (repository.update(book) == 1) {
            redirectAttributes.addFlashAttribute("message", "「" + book.getTitle() + "」を更新しました");
        } else {
            redirectAttributes.addFlashAttribute("message", "その書籍は見つかりませんでした");
        }
        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (repository.deleteById(id) == 1) {
            redirectAttributes.addFlashAttribute("message", "ID " + id + " の書籍を削除しました");
        } else {
            redirectAttributes.addFlashAttribute("message", "その書籍は見つかりませんでした");
        }
        return "redirect:/books";
    }
}
```
### ポイント
- `@ModelAttribute Book book`：フォームの `title`・`author`・`price` が `Book` に詰められる（レッスン07）。
- `RedirectAttributes.addFlashAttribute`：リダイレクト先の画面で **1回だけ** 使える値。一覧画面では普通の `${message}` で読める。
- `update` / `deleteById` の戻り値（変更された行数）が 1 なら成功、0 ならその ID の本は無かった、と判断できる。
- 更新では `book.setId(id)` で **URL の id** を使う。フォームから送られてきた値をそのまま信じない、という習慣をつけておく。

## ステップ3：登録・編集フォーム（`templates/book-form.html`）
登録と編集は、入力欄が同じなので **1つのテンプレート** にする。`book.id` が `null` なら登録、値があれば編集。
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>書籍の登録・編集</title></head>
<body>
  <h1 th:text="${book.id == null} ? '書籍の登録' : '書籍の編集'">書籍の登録</h1>

  <form th:action="${book.id == null} ? @{/books} : @{/books/{id}(id=${book.id})}"
        th:object="${book}" method="post">
    タイトル：<input type="text" th:field="*{title}" size="40"><br>
    著者：<input type="text" th:field="*{author}"><br>
    価格：<input type="number" th:field="*{price}" min="0">円<br>
    <button type="submit" th:text="${book.id == null} ? '登録する' : '更新する'">登録する</button>
  </form>

  <a href="/books">一覧へ戻る</a>
</body>
</html>
```
- `th:object="${book}"` と `th:field="*{title}"`：`name="title"` と `value="（今の値）"` をまとめて出力してくれる。編集のときは今の値が最初から入る。
- 送信先（`th:action`）も、登録なら `/books`、編集なら `/books/3` のように切り替える。

## ステップ4：一覧に「登録」「編集」「削除」を付ける（`templates/books.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"><title>書籍一覧</title>
  <style>
    table { border-collapse: collapse; } th, td { border: 1px solid #999; padding: 4px 8px; }
    .message { color: green; }
    form.inline { display: inline; }
  </style>
</head>
<body>
  <h1>書籍一覧（Thymeleaf 版）</h1>

  <!-- 付録A：登録・更新・削除のあとのメッセージ（フラッシュ属性） -->
  <p class="message" th:if="${message}" th:text="${message}"></p>

  <!-- レッスン14：キーワード検索 -->
  <form action="/books" method="get">
    <input type="text" name="keyword" th:value="${keyword}" placeholder="タイトル・著者">
    <button type="submit">検索</button>
    <a href="/books">クリア</a>
  </form>

  <p th:if="${books.isEmpty()}">書籍が登録されていません。</p>
  <table th:unless="${books.isEmpty()}">
    <tr><th>ID</th><th>タイトル</th><th>著者</th><th>価格</th><th></th><th></th></tr>
    <tr th:each="b : ${books}">
      <td th:text="${b.id}"></td>
      <td><a th:href="@{/books/{id}(id=${b.id})}" th:text="${b.title}"></a></td>
      <td th:text="${b.author}"></td>
      <td th:text="|${#numbers.formatInteger(b.price, 1, 'COMMA')}円|"></td>
      <td><a th:href="@{/books/{id}/edit(id=${b.id})}">編集</a></td>
      <td>
        <form class="inline" th:action="@{/books/{id}/delete(id=${b.id})}" method="post"
              onsubmit="return confirm('削除しますか？');">
          <button type="submit">削除</button>
        </form>
      </td>
    </tr>
  </table>
  <p th:text="|全 ${books.size()} 冊|"></p>
  <p><a href="/books/new">新しく登録する</a></p>
  <a href="/">トップへ</a>
</body>
</html>
```
- 削除は **リンク（GET）ではなくフォーム（POST）** にする。GET はブラウザの先読みや検索エンジンの巡回でも呼ばれることがあり、データを変える操作には使わない。
- `confirm(...)` は「OK」を押したときだけ送信する、ブラウザの確認ダイアログ。

## 動作確認
1. <http://localhost:8080/books> の「新しく登録する」から1冊登録 → 一覧に戻り「「○○」を登録しました」と表示され、一覧に増えている
2. 登録直後に F5 を押しても、同じ本が二重に登録されない（PRG パターン）
3. 「編集」で価格を変えて更新 → 一覧の価格が変わっている
4. 「削除」→ 確認で OK → 一覧から消えている
5. pgAdmin で `SELECT * FROM books ORDER BY id;` を実行し、画面と同じ内容になっていることを確かめる

アプリのログに SQL は出ない。実行されるのは **リポジトリに自分で書いた SQL そのもの** である。

## まだやっていないこと（後のレッスンで学ぶ）
このレッスンの書籍メンテナンスには、わざと手を付けていない所がある。どれも、この先のレッスンで学ぶ機能できれいに解決できる。

| 気になる所 | 今どうなるか | 後で学ぶ機能 |
|---|---|---|
| コントローラがリポジトリを直接呼んでいる | 「空なら全件」「行数が 1 なら成功」などの判断がコントローラに混ざる。リポジトリの作り方を変えるとコントローラも直す必要がある | **サービス層** を挟んで、リポジトリの変更から守る（付録B） |
| SQL や `RowMapper` を毎回手で書いている | `INSERT` / `UPDATE` の列が増えるたびに書き直しが必要 | **Spring Data JPA**（O/R マッパー）なら `save()` / `deleteById()` が最初から用意されている（レッスン16） |
| フォームのクラスが `Book` のまま | フォームから `id` を送れてしまう | **フォーム専用クラス** `BookForm` に分ける（レッスン17） |
| 入力チェックがない | タイトルを空にしても登録できる。価格を空にすると DB の `NOT NULL` 違反でエラー画面になる | **`@Valid` と `@NotBlank` などのアノテーション**（レッスン18） |
| 見つからない ID は一覧に戻すだけ | エラーなのかどうか利用者に分かりにくい | **例外処理と `@ControllerAdvice`** で 404 画面を出す（レッスン18） |
| 誰でも登録・削除できる | URL を知っていれば誰でも書き換えられる | **Spring Security** でログインした人だけに許可する（レッスン19） |

今は「SQL を手で書くと、CRUD はこう動いている」という土台を作る段階である。

## チェックリスト
- [ ] 書き込み（INSERT / UPDATE / DELETE）は `jdbcTemplate.update` を使っている
- [ ] SQL に値を入れるときは `?` を使っている（文字列連結していない）
- [ ] POST のあとは必ず `redirect:` している
- [ ] 削除はリンクではなく POST のフォームで送っている
- [ ] コントローラに SQL を書いていない（SQL はリポジトリだけ）

## 練習問題
1. 詳細ページ（`book-detail.html`）にも「編集」リンクと「削除」ボタンを付ける。
2. 削除のメッセージを「ID 3 の書籍を削除しました」ではなく「「○○」を削除しました」にする。
   ヒント：削除する前に `repository.findById(id)` でタイトルを取っておく。
3. `insert` の戻り値も `int` にして、登録された行数をログ（`System.out.println`）に出してみる。
4. （チャレンジ）登録したばかりの本の ID を知りたい。PostgreSQL の `INSERT ... RETURNING id` を使い、`jdbcTemplate.queryForObject(sql, Integer.class, title, author, price)` で受け取って、登録後に詳細ページ（`/books/{id}`）へリダイレクトする。
