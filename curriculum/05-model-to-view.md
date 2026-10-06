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

## 練習問題
`LocalDateTime.now()` を `hello.html` に渡して現在時刻を表示しよう。
