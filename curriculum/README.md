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
| 依存関係 | webmvc, thymeleaf, validation, lombok, tomcat-embed-jasper（JSP） |
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
| JSP + EL `${...}` | JSP（引き続き利用可）または Thymeleaf `th:text="${...}"` |
