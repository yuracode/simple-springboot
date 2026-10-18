# レッスン16：Spring Data JPA に移行する（JdbcTemplate 版と比較）

## 目標
リポジトリを `JdbcTemplate`（SQL を自分で書く）から **Spring Data JPA**（SQL を自動で作る）に切り替える。
層を分けてあるので、**コントローラとビューは変えなくてよい**。

## 考え方
同じ「キーワード検索」の比較：
```java
// JdbcTemplate 版（レッスン14）：SQL と1行の変換を自分で書く
public List<Book> findByKeyword(String keyword) {
    String sql = "SELECT id, title, author, price FROM books"
            + " WHERE title LIKE ? OR author LIKE ? ORDER BY id";
    String pattern = "%" + keyword + "%";
    return jdbcTemplate.query(sql, bookMapper, pattern, pattern);
}

// JPA 版：メソッドを宣言するだけ（名前から SQL が作られる）
List<Book> findByTitleContainingOrAuthorContainingOrderById(String title, String author);
```
| | JdbcTemplate | Spring Data JPA |
|---|---|---|
| 書くもの | SQL + `RowMapper` | インターフェースだけ |
| `findAll` / `findById` / `save` | 自分で書く | 最初からある |
| 実行される SQL | 書いたとおり | 自動生成（ログで確認） |
| 向いているもの | 複雑な SQL・集計 | 基本的な読み書き |

## ステップ1：依存関係（`pom.xml`）
```xml
<!-- Spring Data JPA (Lesson 16) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

## ステップ2：設定（`application.properties` に追記）
```properties
# レッスン16：Spring Data JPA
# テーブルは schema.sql で作るので、Hibernate には作らせない
spring.jpa.hibernate.ddl-auto=none
# 実行された SQL をコンソールに表示する（学習用）
spring.jpa.show-sql=true
# ビューを表示している間は DB に接続しない（推奨設定）
spring.jpa.open-in-view=false
```

## ステップ3：モデルをエンティティにする（`model/Book.java`）
```java
package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// レッスン12：書籍（books テーブルの1行に対応）
// レッスン16：JPA のエンティティにする
@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // SERIAL（DB が番号を振る）
    private Integer id;
    private String title;
    private String author;
    private Integer price;
}
```
| アノテーション | 意味 |
|---|---|
| `@Entity` / `@Table(name = "books")` | このクラスは `books` テーブルの1行 |
| `@Id` | 主キー |
| `@GeneratedValue(strategy = IDENTITY)` | 番号は DB が振る |

## ステップ4：リポジトリをインターフェースにする（`repository/BookRepository.java`）
クラスの中身をすべて消し、次の内容に書き換える。
```java
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
```
- `JpaRepository<Book, Integer>`：「`Book` を扱う、主キーは `Integer`」。実装クラスは Spring が自動で作る。
- メソッド名の例：`findByAuthor`（`= ?`）、`findByTitleContaining`（`LIKE`）、`findByPriceLessThanEqual`（`<=`）、`…OrderByPriceDesc`。
- 名前を間違える（`findByTitel…`）と **起動時にエラー** になる。

## ステップ5：サービスを JPA 版に合わせる（`service/BookService.java`）
```java
package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

import lombok.RequiredArgsConstructor;

// レッスン13：サービス層（コントローラとリポジトリの間）
// レッスン14：検索の判断をサービスに集める
// レッスン16：JPA 版のリポジトリに切り替える
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;

    public List<Book> findAll() {
        return repository.findAll(Sort.by("id"));   // ORDER BY id
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
}
```
メソッド名（`search`, `findById`）は変わらないので、`BookController` はそのままで動く。

## ステップ6：テストを合わせる（`BookServiceTest.java`）
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
import org.springframework.data.domain.Sort;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

// レッスン15：コンストラクタインジェクションなら、Spring も DB もなしでテストできる
// レッスン16：JPA 版に合わせて書き換え
class BookServiceTest {

    private BookRepository repository;
    private BookService service;

    @BeforeEach
    void setUp() {
        repository = mock(BookRepository.class);   // インターフェースでもモックにできる
        service = new BookService(repository);
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
}
```

## 動作確認
- <http://localhost:8080/books> と <http://localhost:8080/books?keyword=中山> がレッスン14と同じように表示される。
- ターミナルに Hibernate が作った SQL が出る：
  ```
  Hibernate: select b1_0.id,b1_0.author,b1_0.price,b1_0.title from books b1_0 order by b1_0.id
  ```
- `.\mvnw.cmd test "-Dtest=BookServiceTest"` が成功する。

## 練習問題
`findByPriceLessThanEqualOrderByPrice(Integer price)` をリポジトリに追加し、`/books?maxPrice=3000` で絞り込めるようにする。
