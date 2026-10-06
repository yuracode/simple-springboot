# レッスン02：クエリパラメータの受け取り（`@RequestParam`）

## 目標
`?name=...` の値を受け取る。`request.getParameter()` の Spring 版。

## ステップ1：必須パラメータ
`controller/ParamController.java`
```java
@Controller
public class ParamController {

    @GetMapping("/greet")
    @ResponseBody
    public String greet(@RequestParam String name) {
        return "こんにちは、" + name + "さん";
    }
}
```
- <http://localhost:8080/greet?name=太郎> → `こんにちは、太郎さん`
- <http://localhost:8080/greet> → **400 Bad Request**（デフォルトで必須）

## ステップ2：デフォルト値
```java
@GetMapping("/greet2")
@ResponseBody
public String greet2(@RequestParam(defaultValue = "ゲスト") String name) {
    return "こんにちは、" + name + "さん";
}
```

## ステップ3：自動型変換
```java
@GetMapping("/add")
@ResponseBody
public String add(@RequestParam int a, @RequestParam int b) {
    return a + " + " + b + " = " + (a + b);
}
```
- <http://localhost:8080/add?a=3&b=5> → `3 + 5 = 8`
- `?a=x&b=5` → 400。サーブレットでは `Integer.parseInt(...)` を自分で書いていた。

## 考え方
パラメータ名と引数名を一致させる。明示する場合は `@RequestParam("name") String n`。

## 練習問題
`/multiply?a=..&b=..` で掛け算の結果を返そう（デフォルト値 `a=1`, `b=1`）。
