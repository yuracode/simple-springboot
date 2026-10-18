# レッスン18：入力チェック（@Valid）と例外処理

## 目標
- 登録フォームに入力チェックを付け、エラーならメッセージ付きでフォームを再表示する。
- 存在しない本（`/books/999`）を開いたら、**例外を投げて** 404 のエラー画面を出す。

## 考え方
- `@Valid` は Java 標準の入力チェック。レッスン10の `@Validated`（Spring 独自）と、ここでの使い方では同じ動きをする。
- 「見つからない」はサービスが **例外** で知らせ、どんな画面を出すかは **`@ControllerAdvice` の1か所** で決める。コントローラから `if (book == null)` が消える。

## ステップ1：フォームにチェックを付ける（`model/BookForm.java`）
```java
package com.example.demo.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// レッスン17：登録フォームの入力値（エンティティの Book とは分ける）
// レッスン18：入力チェックを付ける
@Data
public class BookForm {

    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    private String title;

    @NotBlank(message = "著者を入力してください")
    @Size(max = 50, message = "著者は50文字以内で入力してください")
    private String author;

    @NotNull(message = "価格を入力してください")
    @Min(value = 0, message = "価格は0円以上で入力してください")
    @Max(value = 100000, message = "価格は100,000円以下で入力してください")
    private Integer price;
}
```
| アノテーション | チェック内容 |
|---|---|
| `@NotNull` | `null` でない |
| `@NotBlank` | 空文字・空白だけでない（文字列用） |
| `@Size(max = 100)` | 文字数（schema.sql の `VARCHAR(100)` に合わせる） |
| `@Min` / `@Max` | 数値の範囲 |

## ステップ2：型変換エラーのメッセージ（`src/main/resources/messages.properties`）
価格に `abc` を入れたときのメッセージ。
```properties
# レッスン18：型変換エラー（例：価格に "abc"）のメッセージ
typeMismatch.java.lang.Integer=数値を入力してください
```

## ステップ3：例外クラス（`exception/BookNotFoundException.java`）
```java
package com.example.demo.exception;

// レッスン18：指定した ID の本がないときに投げる例外
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(int id) {
        super("ID " + id + " の書籍は見つかりませんでした");
    }
}
```

## ステップ4：サービス（`service/BookService.java`）
`findById`（`Optional` を返す）を、見つからなければ例外を投げる `getById` に変える。
```java
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
```

## ステップ5：コントローラ（`controller/BookController.java`）
```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Book;
import com.example.demo.model.BookForm;
import com.example.demo.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// レッスン12〜13：書籍一覧・詳細
// レッスン14：キーワード検索
// レッスン17〜18：登録フォーム + 入力チェック
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
        Book book = service.getById(id);   // 見つからなければ例外 → GlobalExceptionHandler
        model.addAttribute("book", book);
        return "book-detail";
    }

    @GetMapping("/books/new")
    public String newForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        return "book-form";
    }

    @PostMapping("/books")
    public String create(@Valid @ModelAttribute BookForm bookForm, BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "book-form";   // エラーがあればフォームを再表示（入力値は残る）
        }
        Book saved = service.register(bookForm);
        redirectAttributes.addFlashAttribute("message", "「" + saved.getTitle() + "」を登録しました");
        return "redirect:/books";
    }
}
```
`BindingResult` は **チェック対象の引数のすぐ後ろ** に置く。エラーのときは `redirect` せずビュー名を返すので、入力値が残る。

## ステップ6：フォームにエラーを表示（`templates/book-form.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"><title>書籍の登録</title>
  <style>.error { color: red; } input.is-error { border: 2px solid red; }</style>
</head>
<body>
  <h1>書籍の登録</h1>
  <form th:action="@{/books}" th:object="${bookForm}" method="post">
    タイトル：<input type="text" th:field="*{title}" th:errorclass="is-error">
      <span class="error" th:errors="*{title}"></span><br>
    著者：<input type="text" th:field="*{author}" th:errorclass="is-error">
      <span class="error" th:errors="*{author}"></span><br>
    価格：<input type="text" th:field="*{price}" th:errorclass="is-error">円
      <span class="error" th:errors="*{price}"></span><br>
    <button type="submit">登録する</button>
  </form>
  <a href="/books">一覧へ戻る</a>
</body>
</html>
```
`th:errorclass`：エラーがあるときだけ class を追加する（ここでは赤枠）。

## ステップ7：共通の例外ハンドラ（`exception/GlobalExceptionHandler.java`）
```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

// レッスン18：すべてのコントローラに共通の例外処理
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)   // ステータスコード 404 で返す
    public String handleBookNotFound(BookNotFoundException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "book-not-found";
    }
}
```
`@ExceptionHandler(例外.class)`：どこかのコントローラでその例外が起きたら、このメソッドが代わりに呼ばれる。

## ステップ8：エラー画面（`templates/book-not-found.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>書籍が見つかりません</title></head>
<body>
  <h1>書籍が見つかりません</h1>
  <p th:text="${message}">ID xx の書籍は見つかりませんでした</p>
  <a href="/books">一覧へ戻る</a>
</body>
</html>
```

## ステップ9：テストに例外のケースを追加（`BookServiceTest.java`）
```java
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import com.example.demo.exception.BookNotFoundException;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

// レッスン15：コンストラクタインジェクションなら、Spring も DB もなしでテストできる
// レッスン16・18：JPA 版・例外に合わせて書き換え
class BookServiceTest {

    private BookRepository repository;
    private BookService service;

    @BeforeEach
    void setUp() {
        repository = mock(BookRepository.class);   // 本物の代わりの「にせもの」
        service = new BookService(repository);     // コンストラクタで渡すだけ
    }

    @Test
    void キーワードが空なら全件を返す() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(new Book(1, "A", "B", 100)));

        List<Book> result = service.search("  ");

        assertEquals(1, result.size());
        verify(repository, never()).findByTitleContainingOrAuthorContainingOrderById(any(), any());
    }

    @Test
    void キーワードの前後の空白は取り除いて検索する() {
        service.search(" Java ");

        verify(repository).findByTitleContainingOrAuthorContainingOrderById("Java", "Java");
    }

    @Test
    void 存在しないIDなら例外になる() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.getById(999));
    }
}
```

## 動作確認
| 操作 | 結果 |
|---|---|
| <http://localhost:8080/books/new> で何も入れずに登録 | 3項目に赤いメッセージ |
| 価格に `abc` / `-1` | 「数値を入力してください」/「価格は0円以上で…」 |
| タイトルだけ入れてエラー | タイトルの入力値が残っている |
| <http://localhost:8080/books/999> | 「ID 999 の書籍は見つかりませんでした」（F12 のネットワークタブで 404） |

## 練習問題
`src/main/resources/templates/error/404.html` を作り、存在しない URL（例：`/xxx`）でその画面が出ることを確かめる。
