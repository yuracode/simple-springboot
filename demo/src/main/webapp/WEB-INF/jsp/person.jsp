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
  <a href="/">トップへ</a>
</body>
</html>
