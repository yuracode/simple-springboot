# 付録B：サービス層を挟んで、リポジトリの実装変更に備える

## 目標
付録Aで作った書籍のメンテナンスに **サービス層** を挟み、
**リポジトリの実装（SQL の書き方）を丸ごと差し替えても、コントローラとビューを1行も変えずに済む** ことを確かめる。

```
付録A： コントローラ ──▶ BookRepository（JdbcTemplate）
付録B： コントローラ ──▶ BookService ──▶ BookRepository（インターフェース）
                                              ├─ JdbcBookRepository       （JdbcTemplate、付録Aの SQL）
                                              └─ NamedJdbcBookRepository  （NamedParameterJdbcTemplate）
```

## 考え方
付録Aのコントローラには、次のような **判断** が混ざっていた。

| コントローラに書いていた判断 | 本当は誰の仕事か |
|---|---|
| キーワードが空なら `findAll()`、あれば `findByKeyword()` | 業務のルール → **サービス** |
| `update()` の戻り値が `1` なら成功 | 「行数」はリポジトリ（SQL）の都合 → **サービス** で `true` / `false` に直す |

このままリポジトリの作り方を変えると、コントローラまで直すことになる。そこで次の2つを行う。

1. **サービスを挟む**：コントローラは「書籍を登録して」「更新できた？」とサービスに頼むだけにする。
2. **リポジトリをインターフェースにする**：サービスは「`BookRepository` という約束（メソッドの一覧）」だけを知っていて、中身が `JdbcTemplate` なのか別の書き方なのかは知らない。

インターフェースと DI の関係はレッスン15で学んだもの。Spring は `BookRepository` 型のフィールドに、その約束を実装した `@Repository` クラスを **自動で注入** してくれる。

> **メモ：この付録でも O/R マッパーは使わない**
> 差し替える実装は、どちらも SQL を手で書く `JdbcTemplate` 系である。
> レッスン16で学ぶ Spring Data JPA も、見方を変えれば「`BookRepository` の **もう1つの実装**」にすぎない。この付録の形にしておけば、JPA に切り替えるときもサービスより上は変えずに済む。

## 設計
| 層 | ファイル | 付録Aからの変化 |
|---|---|---|
| リポジトリ（約束） | `repository/BookRepository.java` | クラス → **インターフェース** に変える |
| リポジトリ（実装1） | `repository/JdbcBookRepository.java` | **新規**：付録Aの中身をそのまま移す |
| リポジトリ（実装2） | `repository/NamedJdbcBookRepository.java` | **新規**：`NamedParameterJdbcTemplate` 版 |
| サービス | `service/BookService.java` | 判断を集める |
| コントローラ | `controller/BookController.java` | `BookService` だけを使う |
| ビュー | `templates/*.html` | **変えない** |

## ステップ1：リポジトリをインターフェースにする（`repository/BookRepository.java`）
付録Aの `BookRepository` クラスを、**メソッドの宣言だけ** のインターフェースに書き換える。
```java
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.model.Book;

// 付録B：書籍の DB アクセスの「約束」（中身の書き方は実装クラスが決める）
public interface BookRepository {

    List<Book> findAll();

    Optional<Book> findById(int id);

    // タイトルか著者にキーワードを含む本
    List<Book> findByKeyword(String keyword);

    void insert(Book book);

    // 戻り値は更新された行数（0 ならその id は無い）
    int update(Book book);

    // 戻り値は削除された行数（0 ならその id は無い）
    int deleteById(int id);
}
```
### 解説：インターフェースとは
- `class` の代わりに **`interface`** と書く。
- メソッドは **名前・引数・戻り値だけ** を書き、`{ }` の中身（処理）は書かない。最後は `;` で終わる。
- 「`BookRepository` を名乗るなら、この6つのメソッドを必ず用意すること」という **約束（決まりごと）** を表している。
- 中身は、`implements BookRepository` と書いた **実装クラス** が書く（ステップ2・5）。
- `@Repository` はインターフェースには付けない。Spring が `new` して使うのは実装クラスの方なので、実装クラスに付ける。

たとえるなら、インターフェースは「USB の規格」、実装クラスは「USB 規格に合わせて作られたマウス」、使う側のサービスは「パソコン」である。パソコンは USB の規格さえ合っていれば、どのメーカーのマウスが差さっていても同じように使える。マウスを買い替えても、パソコンを作り直す必要はない。

## ステップ2：実装1 ― JdbcTemplate 版（`repository/JdbcBookRepository.java`）
付録Aの `BookRepository` の中身を、そのまま新しいクラスに移す。変わるのは **クラス名と `implements BookRepository`** だけ。
```java
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Book;

import lombok.RequiredArgsConstructor;

// 付録B：BookRepository の実装1（JdbcTemplate、? で値を渡す）
@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final JdbcTemplate jdbcTemplate;

    // 1行 → Book の変換
    private final RowMapper<Book> bookMapper = (rs, rowNum) -> new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getInt("price"));

    @Override
    public List<Book> findAll() {
        String sql = "SELECT id, title, author, price FROM books ORDER BY id";
        return jdbcTemplate.query(sql, bookMapper);
    }

    @Override
    public Optional<Book> findById(int id) {
        String sql = "SELECT id, title, author, price FROM books WHERE id = ?";
        return jdbcTemplate.query(sql, bookMapper, id).stream().findFirst();
    }

    @Override
    public List<Book> findByKeyword(String keyword) {
        String sql = "SELECT id, title, author, price FROM books"
                + " WHERE title LIKE ? OR author LIKE ? ORDER BY id";
        String pattern = "%" + keyword + "%";
        return jdbcTemplate.query(sql, bookMapper, pattern, pattern);
    }

    @Override
    public void insert(Book book) {
        String sql = "INSERT INTO books (title, author, price) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, book.getTitle(), book.getAuthor(), book.getPrice());
    }

    @Override
    public int update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, price = ? WHERE id = ?";
        return jdbcTemplate.update(sql, book.getTitle(), book.getAuthor(), book.getPrice(), book.getId());
    }

    @Override
    public int deleteById(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
```
`@Override` は「インターフェースの約束を実装している」という印。メソッド名や引数を書き間違えるとコンパイルエラーで教えてくれる。

## ステップ3：サービス（`service/BookService.java`）
付録Aでコントローラに書いていた判断を、ここに集める。
```java
package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

// 付録B：サービス層（コントローラとリポジトリの間）
@Service
@RequiredArgsConstructor
public class BookService {

    // インターフェース型で受け取る（どの実装が来るかは Spring が決める）
    private final BookRepository repository;

    // キーワードが空なら全件、あればタイトルか著者で絞り込む
    public List<Book> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return repository.findAll();
        }
        return repository.findByKeyword(keyword.trim());
    }

    public Optional<Book> findById(int id) {
        return repository.findById(id);
    }

    public void register(Book book) {
        repository.insert(book);
    }

    // 更新できたら true、その id が無ければ false
    public boolean update(Book book) {
        return repository.update(book) == 1;
    }

    // 削除できたら true、その id が無ければ false
    public boolean delete(int id) {
        return repository.deleteById(id) == 1;
    }
}
```
- フィールドの型は `JdbcBookRepository` ではなく **`BookRepository`（インターフェース）**。サービスは実装クラスの名前を一度も書いていない。
- 「行数が 1 なら成功」はサービスの中で `boolean` に直す。コントローラは行数という SQL の都合を知らなくてよい。

## ステップ4：コントローラ（`controller/BookController.java`）
`BookRepository` を `BookService` に置き換え、判断を消す。
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
import com.example.demo.service.BookService;

import lombok.RequiredArgsConstructor;

// 付録A：書籍の一覧・詳細・登録・編集・削除
// 付録B：サービスだけを使う
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

    @GetMapping("/books/new")
    public String newForm(Model model) {
        model.addAttribute("book", new Book());   // 空のフォーム
        return "book-form";
    }

    @PostMapping("/books")
    public String create(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        service.register(book);
        redirectAttributes.addFlashAttribute("message", "「" + book.getTitle() + "」を登録しました");
        return "redirect:/books";
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

    @GetMapping("/books/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Book book = service.findById(id).orElse(null);
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
        if (service.update(book)) {
            redirectAttributes.addFlashAttribute("message", "「" + book.getTitle() + "」を更新しました");
        } else {
            redirectAttributes.addFlashAttribute("message", "その書籍は見つかりませんでした");
        }
        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        if (service.delete(id)) {
            redirectAttributes.addFlashAttribute("message", "ID " + id + " の書籍を削除しました");
        } else {
            redirectAttributes.addFlashAttribute("message", "その書籍は見つかりませんでした");
        }
        return "redirect:/books";
    }
}
```
ここまでで起動し、付録Aと **まったく同じ動き** をすることを確かめる（中身の整理だけで、機能は変えていない）。

## ステップ5：実装2 ― NamedParameterJdbcTemplate 版（`repository/NamedJdbcBookRepository.java`）
いよいよリポジトリの実装を差し替える。

`JdbcTemplate` の `?` は **順番** で値を入れるので、列が増えると「何番目の `?` が何か」が分かりにくくなる。
`NamedParameterJdbcTemplate` は `:title` のように **名前** で値を入れられる、Spring の別の部品である（`spring-boot-starter-jdbc` に入っているので、`pom.xml` の追加は不要）。
```java
package com.example.demo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Book;

import lombok.RequiredArgsConstructor;

// 付録B：BookRepository の実装2（NamedParameterJdbcTemplate、:名前 で値を渡す）
@Primary   // BookRepository の実装が2つあるときは、こちらを優先して注入する
@Repository
@RequiredArgsConstructor
public class NamedJdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // 1行 → Book の変換
    private final RowMapper<Book> bookMapper = (rs, rowNum) -> new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getInt("price"));

    @Override
    public List<Book> findAll() {
        String sql = "SELECT id, title, author, price FROM books ORDER BY id";
        return jdbc.query(sql, bookMapper);
    }

    @Override
    public Optional<Book> findById(int id) {
        String sql = "SELECT id, title, author, price FROM books WHERE id = :id";
        return jdbc.query(sql, Map.of("id", id), bookMapper).stream().findFirst();
    }

    @Override
    public List<Book> findByKeyword(String keyword) {
        // 同じ名前は何回使ってもよいので、値は1回渡すだけでよい
        String sql = "SELECT id, title, author, price FROM books"
                + " WHERE title LIKE :pattern OR author LIKE :pattern ORDER BY id";
        return jdbc.query(sql, Map.of("pattern", "%" + keyword + "%"), bookMapper);
    }

    @Override
    public void insert(Book book) {
        // :title は book.getTitle() の値になる（Getter の名前と対応する）
        String sql = "INSERT INTO books (title, author, price) VALUES (:title, :author, :price)";
        jdbc.update(sql, new BeanPropertySqlParameterSource(book));
    }

    @Override
    public int update(Book book) {
        String sql = "UPDATE books SET title = :title, author = :author, price = :price WHERE id = :id";
        return jdbc.update(sql, new BeanPropertySqlParameterSource(book));
    }

    @Override
    public int deleteById(int id) {
        String sql = "DELETE FROM books WHERE id = :id";
        return jdbc.update(sql, Map.of("id", id));
    }
}
```
### 解説1：名前付きパラメータ（`:名前`）とは
実装1では、SQL の中の `?` に **前から順番に** 値が入った。
```java
// 実装1：1つ目の ? に title、2つ目に author、3つ目に price、4つ目に id
String sql = "UPDATE books SET title = ?, author = ?, price = ? WHERE id = ?";
jdbcTemplate.update(sql, book.getTitle(), book.getAuthor(), book.getPrice(), book.getId());
```
順番を1つ間違えると（たとえば `author` と `title` を逆に渡すと）、**エラーにならずに** タイトルと著者が入れ替わって保存されてしまう。

実装2では、`?` の代わりに **`:` + 名前** を書く。名前は自分で自由に決めてよい（`:title` でも `:t` でもよいが、列名と同じにすると分かりやすい）。
```java
// 実装2：:title には「title という名前の値」が入る。順番は関係ない
String sql = "UPDATE books SET title = :title, author = :author, price = :price WHERE id = :id";
```
あとは「どの名前にどの値を入れるか」を、**名前と値の組** で渡せばよい。その渡し方が次の `Map.of(...)` と `BeanPropertySqlParameterSource` である。

### 解説2：`Map.of(...)` で「名前と値の組」を渡す
```java
return jdbc.query(sql, Map.of("pattern", "%" + keyword + "%"), bookMapper);
```
この1行を分解すると、次の3つの部品でできている。

| 引数 | 中身 | 意味 |
|---|---|---|
| 1つ目 | `sql` | `:pattern` を含む SQL |
| 2つ目 | `Map.of("pattern", "%" + keyword + "%")` | 「`pattern` という名前には `%Java%` を入れて」という対応表 |
| 3つ目 | `bookMapper` | 1行 → `Book` の変換（実装1と同じもの） |

#### `Map` とは
`Map` は Java の標準ライブラリにある、**「キー（名前）」と「値」を組にして覚えておく入れ物** である。辞書のように、キーを指定すると値を取り出せる。
```java
Map<String, Object> params = new HashMap<>();
params.put("pattern", "%Java%");     // キー "pattern" に値 "%Java%" を入れる
params.get("pattern");               // → "%Java%"
```

#### `Map.of(キー, 値)` とは
上の3行（`new HashMap<>()` → `put`）を、**1行で書ける** 書き方が `Map.of` である（Java 9 から使える）。
```java
Map.of("pattern", "%Java%")                       // 組が1つ
Map.of("title", "Java入門", "price", 2860)         // 組が2つ（キー, 値, キー, 値, … と交互に並べる）
```
- 引数は **キー, 値, キー, 値, …** の順に交互に並べる。
- `Map.of` で作った Map は **あとから `put` で変更できない**（読み取り専用）。SQL に値を渡すだけなら、それで困らない。
- 値に `null` は入れられない（`NullPointerException` になる）。`null` を入れたい列があるときは `new HashMap<>()` と `put` を使う。

#### SQL のどこに何が入るのか
キーワード検索で `keyword` が `"Java"` のとき、次のように対応づけられる。
```
SQL：  ... WHERE title LIKE :pattern OR author LIKE :pattern ...
                           ↑                         ↑
Map：  "pattern" → "%Java%"  （同じ名前なので、両方に同じ値が入る）

実行される SQL のイメージ：
       ... WHERE title LIKE '%Java%' OR author LIKE '%Java%' ...
```
- Map の **キー**（`"pattern"`）と SQL の **`:` の後ろの名前**（`:pattern`）が一致している必要がある。キーには `:` を付けない。
- 実装1では `?` が2つあったので `pattern, pattern` と2回渡したが、名前付きなら同じ名前を何回使っても値は **1回** 渡せばよい。
- 名前を書き間違えて Map に無い名前が SQL にあると、実行時に「`No value supplied for the SQL parameter 'patern'`」のようなエラーになる（黙って違う値が入ることはない）。
- `?` と同じく、値は **SQL に文字列として埋め込まれるのではなく安全に渡される**。`keyword` に `'` などの記号が入っていても SQL インジェクションにはならない。

`findById` と `deleteById` も同じで、`Map.of("id", id)` は「`:id` には変数 `id` の値を入れて」という意味である。

#### 引数の順番に注意
`JdbcTemplate` と `NamedParameterJdbcTemplate` では、`query` の **引数の順番が違う**。
```java
jdbcTemplate.query(sql, bookMapper, 値1, 値2, ...);   // 実装1：変換が2つ目、値が後ろ
jdbc.query(sql, Map.of(...), bookMapper);             // 実装2：値（Map）が2つ目、変換が最後
```
値の数が決まっていない実装1は「値を最後に好きなだけ並べる」形、値を Map 1つにまとめる実装2は「値を真ん中に置く」形になっている。

### 解説3：`BeanPropertySqlParameterSource` で「オブジェクトの中身」を渡す
`insert` と `update` では、値が `title`・`author`・`price`・`id` と4つもある。`Map.of` で書くと次のようになる。
```java
// Map.of で書いた場合（これでも動く）
jdbc.update(sql, Map.of(
        "title", book.getTitle(),
        "author", book.getAuthor(),
        "price", book.getPrice(),
        "id", book.getId()));
```
`Book` がすでに同じ名前のフィールドを持っているので、これは少しもったいない。そこで使うのが `BeanPropertySqlParameterSource` である。
```java
jdbc.update(sql, new BeanPropertySqlParameterSource(book));
```
- `book` を渡すと、SQL の `:title` を見つけたときに **`book.getTitle()` を呼んで** 値を取ってくれる。`:price` なら `getPrice()`、`:id` なら `getId()`。
- つまり **「`:名前` ⇔ `get名前()`」** という対応で自動的に値が決まる。Getter は Lombok の `@Data` が作っているので、何も追加しなくてよい。
- 名前が Getter と合わないと（たとえば `:bookTitle`）、「そんなプロパティは無い」というエラーになる。

| 値の渡し方 | 向いている場面 | この付録で使っている所 |
|---|---|---|
| `Map.of("名前", 値, ...)` | 値が1〜2個のとき、変数をそのまま渡すとき | `findById`・`findByKeyword`・`deleteById` |
| `new BeanPropertySqlParameterSource(オブジェクト)` | オブジェクトのフィールドをまとめて渡すとき | `insert`・`update` |

### 解説4：`NamedParameterJdbcTemplate` はどこから来るのか
実装1の `JdbcTemplate` と同じく、`spring-boot-starter-jdbc` を入れておくと **Spring Boot が自動で用意してくれる部品** である。だから `private final NamedParameterJdbcTemplate jdbc;` と書いて `@RequiredArgsConstructor` を付けるだけで、DI で注入される（`new` しない）。

### まとめ
| | JdbcTemplate（実装1） | NamedParameterJdbcTemplate（実装2） |
|---|---|---|
| SQL の書き方 | `WHERE id = ?` | `WHERE id = :id` |
| 値の渡し方 | 引数に **順番どおり** 並べる | `Map.of("id", id)` で **名前と値** を渡す |
| オブジェクトをまるごと渡す | できない（1つずつ `book.getTitle()` …） | `new BeanPropertySqlParameterSource(book)` で Getter から自動で取る |
| 同じ値を2回使う | 2回渡す（`pattern, pattern`） | 1回でよい |

### 解説5：`@Primary` が必要な理由
`BookService` は `private final BookRepository repository;` と、**インターフェース型** で注入を待っている。
Spring は起動時に「`BookRepository` を実装した `@Repository` クラス」を探して注入するが、今は次の2つが見つかる。
```
BookRepository を実装したクラス
  ├─ JdbcBookRepository       （@Repository）
  └─ NamedJdbcBookRepository  （@Repository）  ← どっち？
```
このままだと Spring は選べないので、起動時に「`expected single matching bean but found 2`（1つだと思ったら2つ見つかった）」というエラーで止まる。
`@Primary` は「候補が複数あったら **こちらを優先して** ください」という印である。付けた方が注入され、もう一方は使われずに残る。

## ステップ6：差し替わったことを確かめる
起動して、付録Aと同じ操作（一覧・検索・登録・編集・削除）をすべて試す。**同じように動けば成功**である。

このとき、変更したファイルと変更していないファイルを確認する。

| ファイル | ステップ5〜6で変更したか |
|---|---|
| `NamedJdbcBookRepository.java` | **新規作成** |
| `JdbcBookRepository.java` | 変更なし（そのまま残っている） |
| `BookRepository.java`（インターフェース） | 変更なし |
| `BookService.java` | 変更なし |
| `BookController.java` | 変更なし |
| `templates/*.html` | 変更なし |

リポジトリの書き方を丸ごと変えたのに、**サービスもコントローラもビューも1行も変えていない**。これがサービス層とインターフェースで「リポジトリの実装変更に備える」ということである。

元の JdbcTemplate 版に戻したいときは、`@Primary` を `JdbcBookRepository` の方に付け替えるだけでよい。

### 本当に差し替わっている？
`BookService` の `search` の先頭に、一時的に次の1行を追加し、一覧を開いたときのログを見る。
```java
        System.out.println("使っているリポジトリ：" + repository.getClass().getSimpleName());   // 確認用（確かめたら消す）
```
`NamedJdbcBookRepository` と表示されれば、実装2が使われている。

## 付録A と 付録B の比較
| | 付録A | 付録B |
|---|---|---|
| コントローラが使うもの | `BookRepository`（クラス） | `BookService` |
| 判断（空なら全件、行数が1なら成功）の場所 | コントローラ | サービス |
| リポジトリ | クラス1つ | インターフェース＋実装クラス |
| リポジトリの書き方を変えると | コントローラも直す | 新しい実装クラスを作るだけ |
| 向いている場面 | 小さな練習、SQL の動きを学ぶとき | 機能が増えていくアプリ |

## この先で学ぶこと
- **Spring Data JPA（レッスン16）**：`BookRepository` の3つ目の実装として、SQL を自動で作ってくれる O/R マッパーが登場する。この付録の形にしておけば、差し替えの影響はリポジトリの中だけで済む。
- **入力チェック・例外処理（レッスン18）**：「見つからなければ例外を投げて 404 画面を出す」といった判断も、サービスに置くと整理しやすい。

## チェックリスト
- [ ] コントローラは `BookService` だけを使い、`BookRepository` を知らない
- [ ] サービスのフィールドはインターフェース型（`BookRepository`）になっている
- [ ] 実装クラスには `implements BookRepository` と `@Override` が付いている
- [ ] 実装が2つあるときは `@Primary` で優先する方を決めている
- [ ] リポジトリを差し替えても、サービス・コントローラ・ビューを変えていない

## 練習問題
1. `@Primary` を両方から外して起動し、どんなエラーになるか確かめる（エラーメッセージに2つのクラス名が出てくる）。
2. `@Primary` を `JdbcBookRepository` に付け替えて、元の実装に戻ることを確かめる。
3. サービスに `averagePrice()`（平均価格）を追加し、一覧の下に表示する。リポジトリには `SELECT AVG(price) FROM books` を実行するメソッドを、**2つの実装クラスの両方に** 追加する必要がある。インターフェースにメソッドを足すと、実装し忘れたクラスがコンパイルエラーになることも確かめる。
4. （チャレンジ）`@Primary` の代わりに、`application.properties` の `book.repository=named` / `jdbc` で切り替えられるようにする。
   ヒント：実装クラスに `@ConditionalOnProperty(name = "book.repository", havingValue = "named")` を付ける。
