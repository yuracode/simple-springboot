# レッスン06：Lombok を使ったモデルクラス

## 目標
Getter/Setter を手書きせずに JavaBeans 形式のモデルを作る。

## 変更前：普通の JavaBeans
```java
public class Person {
    private String name;
    private int age;
    public Person() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

## 変更後：Lombok（`model/Person.java`）
```java
@Data                 // getter, setter, toString, equals, hashCode を生成
@NoArgsConstructor    // 引数なしコンストラクタ（フォームのバインドに必要）
@AllArgsConstructor   // new Person("太郎", 20)
public class Person {
    private String name;
    private int age;
}
```

## アノテーションを1つずつ試す
VS Code の **アウトライン** ビューで生成されたメソッドを確認しよう。
1. `@Getter` だけ → `getName()` はあるが `setName()` はない。
2. `@Setter` を追加。
3. 両方を `@Data` に置き換える。
4. `@ToString(exclude = "age")` を付けてオブジェクトを表示してみる。

## コントローラで使う（`PersonController`）
```java
@GetMapping("/person")
public String person(Model model) {
    Person p = new Person("太郎", 20);
    p.setAge(p.getAge() + 1);          // Lombok が生成したメソッド
    model.addAttribute("person", p);
    return "person";
}
```
`templates/person.html`
```html
<!DOCTYPE html>
<html lang="ja" xmlns:th="http://www.thymeleaf.org">
<head><meta charset="UTF-8"><title>人物</title></head>
<body>
  <p>名前：<span th:text="${person.name}"></span></p>   <!-- getName() が呼ばれる -->
  <p>年齢：<span th:text="${person.age}"></span></p>    <!-- getAge() が呼ばれる -->
  <p>toString()：<span th:text="${person}"></span></p>  <!-- Lombok の toString() -->
  <a href="/">トップへ</a>
</body>
</html>
```

## うまくいかないとき
- `getName()` に赤い波線が出る：**Extension Pack for Java** を入れ、*Java: Clean Java Language Server Workspace* を実行。
- Maven ビルド用には `pom.xml` で Lombok をアノテーションプロセッサとして登録済み。

## 練習問題
Lombok で `Book`（`title`, `author`, `price`）を作り、1冊表示しよう。
