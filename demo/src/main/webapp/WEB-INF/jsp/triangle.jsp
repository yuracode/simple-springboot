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
