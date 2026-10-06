# レッスン01：一番シンプルなコントローラ（パラメータなし）

## 目標
URL が Java のメソッドとビューにどう結び付くかを理解する。

## 考え方
| サーブレット | Spring |
|---|---|
| `@WebServlet("/hello")` + `doGet()` | `@GetMapping("/hello")` を付けたメソッド |
| `forward("/WEB-INF/jsp/hello.jsp")` | `return "hello";` → `templates/hello.html` |

## ステップ1：ビューを返す（`@Controller`）
`controller/HelloController.java`
```java
@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";   // → src/main/resources/templates/hello.html
    }
}
```

`templates/hello.html`
```html
<!DOCTYPE html>
<html lang="ja">
<head><meta charset="UTF-8"><title>こんにちは</title></head>
<body>
  <h1>こんにちは、Spring Boot！</h1>
</body>
</html>
```

## ステップ2：文字列をそのまま返す（`@ResponseBody`）
```java
@GetMapping("/hello-text")
@ResponseBody
public String helloText() {
    return "テキストでこんにちは";   // 文字列がそのままレスポンスになる
}
```
> `@RestController` = `@Controller` + 全メソッドに `@ResponseBody`。API 向けで、HTML 画面には使わない。

## ステップ3：トップページ
`controller/IndexController.java`
```java
@Controller
public class IndexController {
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
```
`templates/index.html` には各レッスンへのリンクを並べる。

## 動作確認
- <http://localhost:8080/hello>
- <http://localhost:8080/hello-text>
- <http://localhost:8080/>

## 理解度チェック
- `"helo"`（タイプミス）を返すとどうなる？ → テンプレートが見つからずエラー画面。
- `@Controller` と `@RestController` の違いは？

## 練習問題
「このアプリについて」を表示する `/about` を作ろう。
