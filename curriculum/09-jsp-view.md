# レッスン09：Spring Boot で JSP を使う（Thymeleaf と併用）

## 目標
サーブレット/JSP の知識を活かし、Spring のコントローラから JSP を表示する。同じアプリで Thymeleaf も動かす。

> Spring Boot の推奨は Thymeleaf。JSP は実行可能 JAR（`java -jar`）では動かないので、
> **`demo` フォルダーで** `.\mvnw.cmd spring-boot:run` を実行すること
> （Spring Boot は作業フォルダーからの相対パスで `src/main/webapp` を探すため、別の場所で起動すると JSP が 404 になる）。

## ステップ1：依存関係（`pom.xml`）
```xml
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-jasper</artifactId>
</dependency>
<dependency>
    <groupId>jakarta.servlet.jsp.jstl</groupId>
    <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
</dependency>
<dependency>
    <groupId>org.glassfish.web</groupId>
    <artifactId>jakarta.servlet.jsp.jstl</artifactId>
</dependency>
```

## ステップ2：設定（`application.properties`）
ルール：**`jsp/` で始まるビュー名は JSP、それ以外は Thymeleaf。**
```properties
spring.mvc.view.prefix=/WEB-INF/
spring.mvc.view.suffix=.jsp
spring.thymeleaf.excluded-view-names=jsp/*
```
`return "jsp/person";` → `src/main/webapp/WEB-INF/jsp/person.jsp`

## ステップ3：コントローラ（`ViewDemoController`）
```java
@GetMapping("/jsp/person")
public String jspPerson(Model model) {
    model.addAttribute("person", new Person("太郎", 20));
    model.addAttribute("people", samplePeople());
    return "jsp/person";
}
```

## ステップ4：JSP（`src/main/webapp/WEB-INF/jsp/person.jsp`）
```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head><meta charset="UTF-8"><title>JSP</title></head>
<body>
  <h1>JSP からこんにちは</h1>
  <p><c:out value="${person.name}"/>（${person.age}歳）</p>
  <ul>
    <c:forEach var="p" items="${people}">
      <li><c:out value="${p.name}"/> <c:if test="${p.age < 18}">（未成年）</c:if></li>
    </c:forEach>
  </ul>
</body>
</html>
```
- `${person.name}` は Lombok が生成した `getName()` を呼ぶ。教科書の JavaBeans と同じ。
- `Model` の属性はリクエスト属性になるので、`request.setAttribute` と同じように EL から参照できる。

## 動作確認
- <http://localhost:8080/jsp/person> → JSP
- <http://localhost:8080/th-demo> → Thymeleaf

## 練習問題
レッスン07の `person-result.html` を JSP で書き直し、読みやすさを比べてみよう。
