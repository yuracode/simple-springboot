# レッスン05：ビューにデータを渡す（`Model`）

## 目標
文字列を直接返すのをやめ、HTML テンプレートで結果を表示する。

## 考え方
`model.addAttribute("キー", 値)` ≒ `request.setAttribute("キー", 値)`

## ステップ1：コントローラ（`FormController` に追加）
```java
@PostMapping("/form2")
public String submit2(@RequestParam String name, @RequestParam int age, Model model) {
    model.addAttribute("name", name);
    model.addAttribute("age", age);
    model.addAttribute("adult", age >= 18);
    return "result";
}
```

`form.html` の `<a href="/">トップへ</a>` の直前に、送信先 `/form2` のフォームを追加：
```html
  <h2>POST：Model を使って結果画面へ（レッスン05）</h2>
  <form action="/form2" method="post">
    名前：<input type="text" name="name"><br>
    年齢：<input type="number" name="age"><br>
    <button type="submit">送信</button>
  </form>
```

## ステップ2：ビュー（`templates/result.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>結果</title></head>
<body>
  <p>名前：<span th:text="${name}">ダミー</span></p>
  <p>年齢：<span th:text="${age}">0</span></p>
  <p th:if="${adult}">成人です。</p>
  <p th:unless="${adult}">未成年です。</p>
  <a href="/form">戻る</a>
</body>
</html>
```
> タグ内の文字（`ダミー`）は実行時に置き換わる。そのためファイルはブラウザでそのまま開いても HTML として見られる。

## ステップ3：POST 後のリダイレクト（PRG パターン）
```java
@PostMapping("/form3")
public String submit3() {
    return "redirect:/hello";   // ≒ response.sendRedirect()
}
```
`form.html` に追加：
```html
  <h2>POST してリダイレクト（レッスン05）</h2>
  <form action="/form3" method="post">
    <button type="submit">/hello へリダイレクト</button>
  </form>
```

完成した `form.html` 全体：
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

  <h2>POST：Model を使って結果画面へ（レッスン05）</h2>
  <form action="/form2" method="post">
    名前：<input type="text" name="name"><br>
    年齢：<input type="number" name="age"><br>
    <button type="submit">送信</button>
  </form>

  <h2>POST してリダイレクト（レッスン05）</h2>
  <form action="/form3" method="post">
    <button type="submit">/hello へリダイレクト</button>
  </form>
  <a href="/">トップへ</a>
</body>
</html>
```

## 練習問題
`LocalDateTime.now()` を `hello.html` に渡して現在時刻を表示しよう。

<details><summary>解答例</summary>

```java
@GetMapping("/hello")
public String hello(Model model) {
    model.addAttribute("now", LocalDateTime.now());
    return "hello";
}
```
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>こんにちは</title></head>
<body>
  <h1>こんにちは、Spring Boot！</h1>
  <p>現在時刻：<span th:text="${#temporals.format(now, 'yyyy/MM/dd HH:mm:ss')}">時刻</span></p>
  <a href="/">トップへ</a>
</body>
</html>
```
</details>
