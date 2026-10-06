# レッスン03：URL パスの値を受け取る（`@PathVariable`）

## 目標
`/users/42` のように URL の一部を入力値として使う。

## コード（`ParamController` に追加）
```java
@GetMapping("/users/{id}")
@ResponseBody
public String user(@PathVariable int id) {
    return "ユーザーID = " + id;
}

@GetMapping("/square/{n}")
@ResponseBody
public String square(@PathVariable int n) {
    return n + " の2乗 = " + (n * n);
}
```

## 動作確認
- <http://localhost:8080/users/42>
- <http://localhost:8080/square/7>

## `@RequestParam` と `@PathVariable` の使い分け
| | 例 | 主な用途 |
|---|---|---|
| `@RequestParam` | `/search?keyword=java` | 検索条件、フォーム入力 |
| `@PathVariable` | `/users/42` | 特定の1件を指す |

## 練習問題
`/hello/{name}` で `こんにちは、{name}さん！` を返そう。
