# レッスン10：総まとめ — 三角形の面積を求めるプログラム

## 目標
これまでの内容をすべて組み合わせる：フォーム → コントローラ → Lombok モデル → ロジック → ビュー（Thymeleaf と JSP）。

**面積 = 底辺 × 高さ ÷ 2**

## 設計（MVC）
| 層 | ファイル | 役割 |
|---|---|---|
| モデル | `model/Triangle.java` | `base`（底辺）、`height`（高さ）、`area`（面積） |
| ロジック | `service/TriangleService.java` | 計算（教科書の Logic クラスに相当） |
| コントローラ | `controller/TriangleController.java` | GET でフォーム表示、POST で計算 |
| ビュー | `templates/triangle.html`、`WEB-INF/jsp/triangle.jsp` | フォーム + 結果 |

## ステップ1：一番シンプルな版（`@RequestParam`、モデルクラスなし）
```java
@GetMapping("/triangle/simple")
public String simpleForm() {
    return "triangle-simple";
}

@PostMapping("/triangle/simple")
public String simpleCalc(@RequestParam double base, @RequestParam double height, Model model) {
    model.addAttribute("base", base);
    model.addAttribute("height", height);
    model.addAttribute("area", base * height / 2);
    return "triangle-simple";
}
```
`templates/triangle-simple.html`
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>三角形の面積（シンプル版）</title></head>
<body>
  <h1>三角形の面積（シンプル版）</h1>
  <form action="/triangle/simple" method="post">
    底辺：<input type="number" step="any" name="base" required><br>
    高さ：<input type="number" step="any" name="height" required><br>
    <button type="submit">計算する</button>
  </form>
  <p th:if="${area != null}" th:text="|${base} × ${height} ÷ 2 = ${area}|"></p>
  <a href="/">トップへ</a>
</body>
</html>
```

## ステップ2：Lombok モデル
```java
@Data
public class Triangle {
    @NotNull(message = "底辺を入力してください")
    @Positive(message = "底辺は0より大きい値を入力してください")
    private Double base;

    @NotNull(message = "高さを入力してください")
    @Positive(message = "高さは0より大きい値を入力してください")
    private Double height;

    private Double area;
}
```
（入力チェックのアノテーションは `spring-boot-starter-validation` が必要。最初は付けずに作り、ステップ5で追加する。）

## ステップ3：ロジッククラス
```java
@Service
public class TriangleService {
    public void calcArea(Triangle t) {
        t.setArea(t.getBase() * t.getHeight() / 2);
    }
}
```

## ステップ4：フォームバインド + DI を使ったコントローラ
```java
@Controller
@RequiredArgsConstructor               // Lombok：コンストラクタインジェクション
public class TriangleController {

    private final TriangleService service;

    @GetMapping("/triangle")
    public String form(Model model) {
        model.addAttribute("triangle", new Triangle());
        return "triangle";
    }

    @PostMapping("/triangle")
    public String calc(@Validated @ModelAttribute Triangle triangle, BindingResult result) {
        if (!result.hasErrors()) {
            service.calcArea(triangle);
        }
        return "triangle";
    }
}
```
`templates/triangle.html`
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"><title>三角形の面積</title>
  <style>.error { color: red; }</style>
</head>
<body>
  <h1>三角形の面積（Thymeleaf 版）</h1>
  <form th:action="@{/triangle}" th:object="${triangle}" method="post">
    底辺：<input type="number" step="any" th:field="*{base}">
          <span th:errors="*{base}" class="error"></span><br>
    高さ：<input type="number" step="any" th:field="*{height}">
          <span th:errors="*{height}" class="error"></span><br>
    <button type="submit">計算する</button>
  </form>
  <p th:if="${triangle.area != null}"
     th:text="|面積：${#numbers.formatDecimal(triangle.area, 1, 2)}|"></p>
  <a href="/">トップへ</a>
</body>
</html>
```
（`th:errors` はステップ5の入力チェックを入れるまでは何も表示されない）

## ステップ5：入力チェック
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
`@Validated` + `BindingResult`（チェック対象の引数の直後に置く）で、400 エラー画面ではなく
`th:errors` でメッセージを表示できる。

## ステップ6：JSP 版
```java
@GetMapping("/jsp/triangle")
public String jspForm(Model model) {
    model.addAttribute("triangle", new Triangle());
    return "jsp/triangle";
}

@PostMapping("/jsp/triangle")
public String jspCalc(@Validated @ModelAttribute Triangle triangle, BindingResult result) {
    if (!result.hasErrors()) {
        service.calcArea(triangle);
    }
    return "jsp/triangle";
}
```
`WEB-INF/jsp/triangle.jsp`
```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head><meta charset="UTF-8"><title>三角形の面積（JSP 版）</title></head>
<body>
  <h1>三角形の面積（JSP 版）</h1>
  <form action="/jsp/triangle" method="post">
    底辺：<input type="number" step="any" name="base"   value="${triangle.base}"><br>
    高さ：<input type="number" step="any" name="height" value="${triangle.height}"><br>
    <button type="submit">計算する</button>
  </form>
  <c:if test="${not empty triangle.area}">
    <p>面積：<fmt:formatNumber value="${triangle.area}" maxFractionDigits="2"/></p>
  </c:if>
  <a href="/">トップへ</a>
</body>
</html>
```

## ステップ7：単体テスト
```java
class TriangleServiceTest {
    @Test
    void area() {
        Triangle t = new Triangle();
        t.setBase(10.0);
        t.setHeight(5.0);
        new TriangleService().calcArea(t);
        assertEquals(25.0, t.getArea());
    }
}
```
実行：`.\mvnw.cmd test`

## チェックリスト
- [ ] 未入力のとき 400 画面ではなくエラーメッセージが出る
- [ ] マイナスの値ははじかれる
- [ ] 小数（例：2.5）も計算できる
- [ ] Thymeleaf と JSP で同じ結果になる

## チャレンジ
- 長方形・円も選べるようにし、`ShapeService` を作る。
- 計算履歴を `HttpSession` に保存し、`th:each` で一覧表示する。
