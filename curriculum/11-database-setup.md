# レッスン11：PostgreSQL に接続する準備

## 目標
Spring Boot から PostgreSQL（`localhost:5432` のデータベース `springboot`）に接続できるようにする。

| 項目 | 値 |
|---|---|
| ホスト / ポート | `localhost` / `5432` |
| データベース | `springboot` |
| ユーザー名 / パスワード | `postgres` / `postgres` |

## 考え方
サーブレット/JSP では、DAO の中で毎回 `DriverManager.getConnection(URL, USER, PASS)` を書いていた。
Spring Boot では **接続情報を `application.properties` に書くだけ**で、接続（`DataSource`）は Spring が用意してくれる。

## ステップ1：データベースを作る
pgAdmin か psql で、次の SQL を **1回だけ**実行する（すでにあれば不要）。
```sql
CREATE DATABASE springboot;
```

## ステップ2：依存関係（`pom.xml`）
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```
- `spring-boot-starter-jdbc`：`JdbcTemplate` と接続プール（HikariCP）
- `postgresql`：JDBC ドライバ（実行時だけ必要なので `runtime`）

## ステップ3：接続設定（`application.properties`）
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/springboot
spring.datasource.username=postgres
spring.datasource.password=postgres
# 起動時に schema.sql（テーブル作成）と data.sql（サンプルデータ）を実行する
spring.sql.init.mode=always
```

## ステップ4：テーブルと初期データ（`src/main/resources`）
`src/main/resources/schema.sql`
```sql
-- レッスン11：書籍テーブル
CREATE TABLE IF NOT EXISTS books (
    id     SERIAL PRIMARY KEY,
    title  VARCHAR(100) NOT NULL,
    author VARCHAR(50)  NOT NULL,
    price  INTEGER      NOT NULL
);
```
`src/main/resources/data.sql`
```sql
-- レッスン11：サンプルデータ（テーブルが空のときだけ入れる）
INSERT INTO books (title, author, price)
SELECT * FROM (VALUES
    ('スッキリわかるJava入門', '中山清喬', 2860),
    ('スッキリわかるサーブレット&JSP入門', '国本大悟', 3080),
    ('スッキリわかるSQL入門', '中山清喬', 2860),
    ('Spring徹底入門', 'NTTデータ', 4400)
) AS v(title, author, price)
WHERE NOT EXISTS (SELECT 1 FROM books);
```
`spring.sql.init.mode=always` なので、**起動のたびに**実行される。そのため、
- テーブルは `IF NOT EXISTS`（すでにあれば作らない）
- データは `WHERE NOT EXISTS`（テーブルが空のときだけ入れる）

として、何回起動しても同じ状態になるようにしている。

## 動作確認
`.\mvnw.cmd spring-boot:run` がエラーなく起動すれば OK。pgAdmin で `SELECT * FROM books;` を実行すると 4 件入っている。

よくあるエラー：
| メッセージ | 原因 |
|---|---|
| `database "springboot" does not exist` | ステップ1をやっていない |
| `password authentication failed` | ユーザー名かパスワードが間違っている |
| `Connection refused` | PostgreSQL が起動していない／ポートが違う |

> 注意：ここから先は、**PostgreSQL が動いていないとアプリ自体が起動しない**（`.\mvnw.cmd test` も失敗する）。

## 練習問題
1. `data.sql` に好きな本を1冊追加する。テーブルを空にして（`DELETE FROM books;`）から再起動するとどうなるか確かめる。
2. `spring.sql.init.mode=never` にすると、SQL が実行されなくなることを確かめる。
