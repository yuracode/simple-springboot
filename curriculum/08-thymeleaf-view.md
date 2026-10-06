# レッスン08：Thymeleaf の基本

## コントローラ（`ViewDemoController`）
```java
@GetMapping("/th-demo")
public String thDemo(Model model) {
    model.addAttribute("title", "Thymeleaf デモ");
    model.addAttribute("score", 75);
    model.addAttribute("people", samplePeople());
    return "th-demo";
}
```

## `templates/th-demo.html`
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title th:text="${title}">タイトル</title></head>
<body>
  <!-- 1. 出力（HTML エスケープされる） -->
  <h1 th:text="${title}">タイトル</h1>

  <!-- 2. 条件分岐 -->
  <p th:if="${score >= 60}">合格</p>
  <p th:unless="${score >= 60}">不合格</p>

  <!-- 3. 三項演算子 -->
  <p th:text="${score >= 80} ? '優秀' : '普通'"></p>

  <!-- 4. 繰り返し（st はループの状態：count, index, first, last など） -->
  <table border="1">
    <tr><th>No.</th><th>名前</th><th>年齢</th></tr>
    <tr th:each="p, st : ${people}">
      <td th:text="${st.count}"></td>
      <td th:text="${p.name}"></td>
      <td th:text="${p.age}"></td>
    </tr>
  </table>

  <!-- 5. パラメータ付きリンク → /greet?name=太郎 -->
  <a th:href="@{/greet(name=${people[0].name})}">1人目にあいさつ</a> |
  <a href="/">トップへ</a>
</body>
</html>
```

## 早見表
| 目的 | Thymeleaf | JSP（EL/JSTL） |
|---|---|---|
| 出力 | `th:text="${x}"` | `<c:out value="${x}"/>` |
| 条件分岐 | `th:if` / `th:unless` | `<c:if test="...">` |
| 繰り返し | `th:each="i : ${list}"` | `<c:forEach var="i" items="${list}">` |
| URL | `@{/path(k=${v})}` | `<c:url>` |
| フォームオブジェクト | `th:object` + `*{field}` | — |

## 練習問題
`people` が空のとき「データがありません」と表示しよう。
