# Spring Boot スモールステップ・カリキュラム

**VS Code** と **Maven** を使って Spring Boot の Web アプリを少しずつ作っていくカリキュラムです。
『スッキリわかるサーブレット＆JSP入門 第5版』第15章の内容（サーブレット/JSP から Spring へ：コントローラ・モデル・ビュー）に沿っています。

全レッスンの完成コードは `../demo` にあります。

## プロジェクトの前提（`demo/`）

| 項目 | 値 |
|---|---|
| ビルドツール | Maven（`mvnw` / `mvnw.cmd`） |
| Spring Boot | 4.1.1 |
| Java | 21 |
| 依存関係 | webmvc, thymeleaf, validation, lombok, tomcat-embed-jasper（JSP）, jdbc, postgresql, data-jpa（レッスン16〜）, security（レッスン19〜） |
| データベース | PostgreSQL `localhost:5432/springboot`（postgres / postgres）※レッスン11以降 |
| ベースパッケージ | `com.example.demo` |

## レッスン一覧

| # | ファイル | 目標 |
|---|---|---|
| 00 | [00-setup.md](00-setup.md) | VS Code + Maven の準備とアプリの起動 |
| 01 | [01-simple-controller.md](01-simple-controller.md) | 一番シンプルなコントローラ（パラメータなし） |
| 02 | [02-request-param.md](02-request-param.md) | `@RequestParam` でクエリパラメータを受け取る |
| 03 | [03-path-variable.md](03-path-variable.md) | `@PathVariable` で URL の一部を受け取る |
| 04 | [04-form-get-post.md](04-form-get-post.md) | HTML フォーム → GET / POST → コントローラ |
| 05 | [05-model-to-view.md](05-model-to-view.md) | `Model` でビューにデータを渡す |
| 06 | [06-lombok-model.md](06-lombok-model.md) | Lombok で Getter/Setter を自動生成するモデル |
| 07 | [07-form-binding.md](07-form-binding.md) | フォームをオブジェクトで受け取る（`@ModelAttribute`） |
| 08 | [08-thymeleaf-view.md](08-thymeleaf-view.md) | Thymeleaf の基本 |
| 09 | [09-jsp-view.md](09-jsp-view.md) | JSP + JSTL/EL を Thymeleaf と併用する |
| 10 | [10-triangle-area.md](10-triangle-area.md) | 総まとめ：三角形の面積を求めるプログラム |
| 11 | [11-database-setup.md](11-database-setup.md) | PostgreSQL に接続する準備（schema.sql / data.sql） |
| 12 | [12-book-list.md](12-book-list.md) | `JdbcTemplate` で書籍一覧を表示する |
| 13 | [13-book-service-detail.md](13-book-service-detail.md) | サービス層を挟む + 書籍の詳細ページ |
| 14 | [14-service-layer.md](14-service-layer.md) | サービス層に処理を集める（キーワード検索） |
| 15 | [15-dependency-injection.md](15-dependency-injection.md) | DI とコンストラクタインジェクション |
| 16 | [16-spring-data-jpa.md](16-spring-data-jpa.md) | Spring Data JPA に移行する（JdbcTemplate 版と比較） |
| 17 | [17-book-register-form.md](17-book-register-form.md) | Thymeleaf で登録フォーム（フォーム → 登録 → 一覧） |
| 18 | [18-validation-exception.md](18-validation-exception.md) | 入力チェック（`@Valid`）と例外処理（`@ControllerAdvice`） |
| 19 | [19-spring-security.md](19-spring-security.md) | Spring Security でログインする |
| 20 | [20-final-exercise.md](20-final-exercise.md) | 最終演習：ToDo リストを作る（問題のみ） |

## 進め方

1. **目標** と **考え方** を読む。
2. コードは自分の手で入力する。
3. `.\mvnw.cmd spring-boot:run` で起動し、**動作確認** の URL を開く。
4. **練習問題** を解いてから次へ進む。

## サーブレット/JSP との対応表

| サーブレット/JSP | Spring Boot |
|---|---|
| `HttpServlet` + `@WebServlet("/x")` | `@Controller` + `@GetMapping("/x")` |
| `doGet` / `doPost` | `@GetMapping` / `@PostMapping` |
| `request.getParameter("name")` | `@RequestParam String name` |
| `request.setAttribute("k", v)` | `model.addAttribute("k", v)` |
| `RequestDispatcher.forward(...)` | `return "ビュー名";` |
| `response.sendRedirect(...)` | `return "redirect:/パス";` |
| JavaBeans（Getter/Setter を手書き） | Lombok の `@Data` |
| モデルの「Logic」クラス | コントローラに注入する `@Service` クラス |
| DAO（`DriverManager` + `PreparedStatement`） | `@Repository` + `JdbcTemplate`、または Spring Data JPA の `JpaRepository` |
| Logic クラスを `new` して使う | コンストラクタインジェクション（DI） |
| 入力チェックを `if` で手書き | `@Valid` + `@NotBlank` などのアノテーション |
| `try-catch` してエラーページへ `forward` | `@ExceptionHandler` / `@ControllerAdvice` |
| セッション + フィルタでログインチェック | Spring Security |
| JSP + EL `${...}` | JSP（引き続き利用可）または Thymeleaf `th:text="${...}"` |
