# レッスン04：HTML フォームと GET / POST

## 目標
フォームを表示して送信し、コントローラで受け取る。**GET（フォーム表示）→ POST（処理）** の流れを覚える。

## ステップ1：コントローラ
`controller/FormController.java`
```java
@Controller
public class FormController {

    @GetMapping("/form")
    public String showForm() {
        return "form";
    }

    @PostMapping("/form")
    @ResponseBody
    public String submit(@RequestParam String name, @RequestParam int age) {
        return name + "さんは" + age + "歳です。";
    }
}
```

## ステップ2：フォーム（`templates/form.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>フォーム</title></head>
<body>
  <h2>POST：テキストを返す（レッスン04）</h2>
  <form action="/form" method="post">
    名前：<input type="text" name="name"><br>
    年齢：<input type="number" name="age"><br>
    <button type="submit">送信</button>
  </form>
  <a href="/">トップへ</a>
</body>
</html>
```
> `<input>` の `name` 属性と `@RequestParam` の名前を一致させること。

## ステップ3：GET 版
`form.html` の `<a href="/">トップへ</a>` の直前に追加：
```html
  <h2>GET：検索（レッスン04）</h2>
  <form action="/search" method="get">
    キーワード：<input type="text" name="keyword">
    <button type="submit">検索</button>
  </form>
```
追加後の `form.html` 全体：
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>フォーム</title></head>
<body>
  <h2>POST：テキストを返す（レッスン04）</h2>
  <form action="/form" method="post">
    名前：<input type="text" name="name"><br>
    年齢：<input type="number" name="age"><br>
    <button type="submit">送信</button>
  </form>

  <h2>GET：検索（レッスン04）</h2>
  <form action="/search" method="get">
    キーワード：<input type="text" name="keyword">
    <button type="submit">検索</button>
  </form>
  <a href="/">トップへ</a>
</body>
</html>
```
```java
@GetMapping("/search")
@ResponseBody
public String search(@RequestParam String keyword) {
    return "検索キーワード：" + keyword;
}
```
入力値が URL に表示されることを確認しよう。

## 考え方
| | GET | POST |
|---|---|---|
| データの場所 | URL のクエリ文字列 | リクエストボディ |
| ブックマーク | できる | できない |
| 用途 | 検索・表示 | 登録・更新 |

- `@GetMapping("/form")` と `@PostMapping("/form")` は共存できる（`doGet`/`doPost` と同じ）。
- Spring Boot は既定で UTF-8 なので `request.setCharacterEncoding()` は不要。日本語もそのまま扱える。

## 練習問題
`<select name="lang">`（Java / Python / JS）を追加し、選んだ値も表示しよう。
