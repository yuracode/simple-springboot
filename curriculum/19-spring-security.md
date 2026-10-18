# レッスン19：Spring Security でログインする

## 目標
- 書籍の **登録はログインした人だけ**、一覧・詳細は誰でも見られるようにする。
- 自作のログイン画面・ログアウトを作り、一覧にログイン中のユーザー名を表示する。

学習用ユーザー：`user` / `password`

## 考え方
教科書では、ログイン状態を `HttpSession` に入れ、サーブレットやフィルタで自分でチェックしていた。
Spring Security は **コントローラの手前のフィルタ** で、これをまとめて行う。
```
ブラウザ ─▶ [Spring Security] ─▶ コントローラ
             ├ ログインしているか（認証）
             ├ この URL を使ってよいか（認可）
             └ CSRF トークンは正しいか
```

## ステップ1：依存関係（`pom.xml`）
```xml
<!-- Spring Security (Lesson 19) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
ここで一度起動すると、**どのページもログイン画面に飛ばされる**（何も設定しないと全ページがログイン必須）。
次のステップで、守るページを自分で決める。

## ステップ2：セキュリティの設定（`config/SecurityConfig.java`）
```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// レッスン19：Spring Security の設定
@Configuration
public class SecurityConfig {

    // どの URL にログインが必要かを決める
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/books/new").authenticated()                 // 登録フォーム
                .requestMatchers(HttpMethod.POST, "/books").authenticated()    // 登録処理
                .anyRequest().permitAll())                                     // それ以外は誰でも
            .formLogin(form -> form
                .loginPage("/login")             // 自作のログイン画面
                .defaultSuccessUrl("/books")     // ログイン後の移動先（元のページがあればそちら）
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout"));
        return http.build();
    }

    // パスワードはハッシュ化して保存する
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 学習用：メモリ上にユーザーを1人用意する（本番では DB から読み込む）
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.withUsername("user")
                .password(encoder.encode("password"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```
- `@Configuration` + `@Bean`：メソッドの戻り値を Bean として登録する（レッスン15の「Spring が作って渡す部品」を自分で作る書き方）。
- `authorizeHttpRequests` は **上から順に** 当てはめる。最後の `anyRequest()` は「それ以外全部」。
- パスワードはそのまま保存せず、`PasswordEncoder` でハッシュ化（元に戻せない形に変換）しておく。

## ステップ3：ログイン画面（`controller/LoginController.java`、`templates/login.html`）
```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// レッスン19：ログイン画面を表示するだけ（認証の処理は Spring Security が行う）
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
```
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"><title>ログイン</title>
  <style>.error { color: red; } .info { color: green; }</style>
</head>
<body>
  <h1>ログイン</h1>
  <p class="error" th:if="${param.error}">ユーザー名またはパスワードが違います。</p>
  <p class="info" th:if="${param.logout}">ログアウトしました。</p>
  <!-- th:action を使うと CSRF トークンの hidden 項目が自動で入る -->
  <form th:action="@{/login}" method="post">
    ユーザー名：<input type="text" name="username" autofocus><br>
    パスワード：<input type="password" name="password"><br>
    <button type="submit">ログイン</button>
  </form>
  <p>学習用ユーザー：user / password</p>
  <a href="/books">書籍一覧へ</a>
</body>
</html>
```
`POST /login`（パスワードの確認）は自分で書かない。Spring Security が `username` と `password` を受け取って行う。

## ステップ4：CSRF 対策 — POST のフォームを `th:action` にする
Spring Security は POST に **CSRF トークン**（画面ごとに発行される合言葉）が付いているかをチェックし、なければ **403** にする。
Thymeleaf は `th:action` のフォームにだけトークンを自動で入れるので、レッスン04・10 の `action="..."` を直す。

`templates/form.html`
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>フォーム</title></head>
<body>
  <h2>POST：テキストを返す（レッスン04）</h2>
  <form th:action="@{/form}" method="post">
    名前：<input type="text" name="name"><br>
    年齢：<input type="number" name="age"><br>
    <button type="submit">送信</button>
  </form>

  <h2>GET：検索（レッスン04）</h2>
  <form action="/search" method="get">
    キーワード：<input type="text" name="keyword">
    <button type="submit">検索</button>
  </form>

  <h2>POST：Model を使って結果画面へ（レッスン05）</h2>
  <form th:action="@{/form2}" method="post">
    名前：<input type="text" name="name"><br>
    年齢：<input type="number" name="age"><br>
    <button type="submit">送信</button>
  </form>

  <h2>POST してリダイレクト（レッスン05）</h2>
  <form th:action="@{/form3}" method="post">
    <button type="submit">/hello へリダイレクト</button>
  </form>
  <a href="/">トップへ</a>
</body>
</html>
```
`templates/triangle-simple.html`
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>三角形の面積（シンプル版）</title></head>
<body>
  <h1>三角形の面積（シンプル版）</h1>
  <form th:action="@{/triangle/simple}" method="post">
    底辺：<input type="number" step="any" name="base" required><br>
    高さ：<input type="number" step="any" name="height" required><br>
    <button type="submit">計算する</button>
  </form>
  <p th:if="${area != null}" th:text="|${base} × ${height} ÷ 2 = ${area}|"></p>
  <a href="/">トップへ</a>
</body>
</html>
```
GET のフォームはチェックされないので、そのままでよい。

## ステップ5：一覧にログイン状態を表示する
`controller/BookController.java`（`list` に `Principal` を追加）
```java
package com.example.demo.controller;

import java.security.Principal;

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
// レッスン19：ログイン中のユーザー名を表示
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String keyword,
                       Principal principal, Model model) {
        model.addAttribute("books", service.search(keyword));
        model.addAttribute("keyword", keyword);
        // ログインしていなければ principal は null
        model.addAttribute("username", principal == null ? null : principal.getName());
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
`templates/books.html`（`<body>` の先頭にログイン状態を追加）
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
  <!-- レッスン19：ログイン状態 -->
  <div th:if="${username != null}">
    ログイン中：<span th:text="${username}"></span>
    <form th:action="@{/logout}" method="post" style="display:inline">
      <button type="submit">ログアウト</button>
    </form>
  </div>
  <div th:if="${username == null}"><a href="/login">ログイン</a></div>

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
ログアウトも POST（CSRF トークンが必要）なので、リンクではなく `th:action` のフォームにする。

## 動作確認
| 操作 | 結果 |
|---|---|
| ログインせずに <http://localhost:8080/books> | 一覧が見られる（上に「ログイン」リンク） |
| ログインせずに「新しい本を登録する」 | ログイン画面へ |
| パスワードを間違える | 「ユーザー名またはパスワードが違います。」 |
| `user` / `password` でログイン | 開こうとしていた登録フォームに移動 |
| 「ログアウト」 | 「ログアウトしました。」 |
| <http://localhost:8080/triangle/simple> で計算 | 403 にならずに計算できる |

## 練習問題
登録メッセージにユーザー名を入れる（「user さんが「○○」を登録しました」）。ヒント：`create` に `Principal principal` を追加。
