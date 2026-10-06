# レッスン07：フォームをオブジェクトで受け取る（`@ModelAttribute`）

## 目標
`@RequestParam` を並べる代わりに、フォーム全体を1つのオブジェクトで受け取る。

## ステップ1：コントローラ（`PersonController` に追加）
```java
@GetMapping("/person/form")
public String form(Model model) {
    model.addAttribute("person", new Person());   // フォーム用の空オブジェクト
    return "person-form";
}

@PostMapping("/person/form")
public String submit(@ModelAttribute Person person) {
    // Spring が new Person() → setName() → setAge() を代わりに実行してくれる
    // @ModelAttribute により "person" という名前でモデルにも入る
    return "person-result";
}
```

## ステップ2：フォーム（`templates/person-form.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>人物の入力</title></head>
<body>
  <form th:action="@{/person/form}" th:object="${person}" method="post">
    名前：<input type="text" th:field="*{name}"><br>
    年齢：<input type="number" th:field="*{age}"><br>
    <button type="submit">送信</button>
  </form>
</body>
</html>
```
- `th:field="*{name}"` は `id="name" name="name" value="..."` を生成する。
- `@{...}` は URL を組み立てる。

## ステップ3：結果（`templates/person-result.html`）
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>入力結果</title></head>
<body>
  <p th:text="|${person.name}さんは${person.age}歳です。|"></p>
  <a href="/person/form">戻る</a>
</body>
</html>
```
（`|...|` はリテラル置換。文字列と `${...}` を `+` なしでつなげられる）

## 考え方
バインドの流れ：リクエストパラメータ → `new Person()` → `setName(...)`, `setAge(...)` → コントローラの引数。
だからモデルには **引数なしコンストラクタと Setter** が必要。

## 練習問題
`Person` とフォームに `email` を追加しよう。Getter/Setter を書く必要はない。
