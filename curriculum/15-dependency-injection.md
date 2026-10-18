# レッスン15：DI とコンストラクタインジェクション

## 目標
- **DI（依存性の注入）** の仕組みを知る。
- **コンストラクタインジェクション** を使う理由を知り、Spring も DB もなしでサービスをテストする。

## 考え方
```java
// DI なし：使う側が作り方まで知っていないといけない
private final BookService service = new BookService(new BookRepository(new JdbcTemplate(...)));

// DI あり：「ください」と宣言するだけ
private final BookService service;
```
起動時に Spring が `@Controller` `@Service` `@Repository` の付いたクラスを1つずつ作り（**Bean**）、コンストラクタの引数に渡して組み立てる。

```
JdbcTemplate ─▶ BookRepository ─▶ BookService ─▶ BookController
```

### 注入の3つの書き方
```java
// ① フィールドインジェクション（非推奨）
@Autowired
private BookRepository repository;

// ② セッターインジェクション
@Autowired
public void setRepository(BookRepository repository) { this.repository = repository; }

// ③ コンストラクタインジェクション（推奨）
private final BookRepository repository;
public BookService(BookRepository repository) { this.repository = repository; }
```
③ をすすめる理由：
- `final` にできる（あとから差し替えられない）
- 渡し忘れが **コンパイルエラー** になる
- テストで `new BookService(にせもの)` と書ける

これまで使ってきた `@RequiredArgsConstructor` は、③ のコンストラクタを Lombok が自動で書いているだけ。

## ステップ1：コンストラクタを手で書いてみる（`service/BookService.java`）
`@RequiredArgsConstructor` を消し、同じ意味のコンストラクタを書く。
```java
package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

// レッスン13：サービス層（コントローラとリポジトリの間）
// レッスン14：検索の判断をサービスに集める
// レッスン15：コンストラクタを手で書く（@RequiredArgsConstructor と同じ意味）
@Service
public class BookService {

    private final BookRepository repository;

    // コンストラクタが1つだけなら @Autowired は不要
    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll() {
        return repository.findAll();
    }

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
起動して、レッスン14と同じように動くことを確かめたら、`@RequiredArgsConstructor` に戻してよい（以降のレッスンは `@RequiredArgsConstructor` で書く）。

## ステップ2：にせものを渡してテストする（`src/test/java/com/example/demo/service/BookServiceTest.java`）
Mockito（`spring-boot-starter-webmvc-test` に入っている）で、リポジトリの **モック（にせもの）** を作って渡す。
```java
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

// レッスン15：コンストラクタインジェクションなら、Spring も DB もなしでテストできる
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
        when(repository.findAll()).thenReturn(List.of(new Book(1, "A", "B", 100)));

        List<Book> result = service.search("  ");

        assertEquals(1, result.size());
        verify(repository, never()).findByKeyword(any());   // 検索 SQL は呼ばれない
    }

    @Test
    void キーワードの前後の空白は取り除いて検索する() {
        service.search(" Java ");

        verify(repository).findByKeyword("Java");
    }
}
```
| 書き方 | 意味 |
|---|---|
| `mock(クラス.class)` | にせものを作る |
| `when(呼び出し).thenReturn(値)` | 呼ばれたら返す値を決めておく |
| `verify(モック).メソッド(引数)` | その引数で呼ばれたか確かめる |

## 動作確認
```powershell
.\mvnw.cmd test "-Dtest=BookServiceTest"
```
`Tests run: 2, Failures: 0` なら OK（Spring も PostgreSQL も起動しないので一瞬で終わる）。
サービスの `keyword.trim()` を `keyword` に変えると2つ目が失敗することも確かめる。

## 練習問題
`BookController` も `@RequiredArgsConstructor` を消してコンストラクタを手で書いてみる。
