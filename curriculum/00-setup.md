# レッスン00：環境準備と初回起動

## 目標
Maven プロジェクトを VS Code で開き、Spring Boot アプリを起動する。

## 前提
- JDK 21（`java -version` で確認）
- VS Code 拡張機能：**Extension Pack for Java**、**Spring Boot Extension Pack**
- Spring Initializr で作成したプロジェクト（Maven / Java 21 / 依存関係：Spring Web, Thymeleaf, Lombok）

## 手順
1. VS Code で *ファイル → フォルダーを開く…* → `demo` を選ぶ。
2. Java 拡張機能による Maven プロジェクトの読み込み完了を待つ。
3. ターミナルを開いて実行：
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. `Tomcat started on port 8080` と表示されたら <http://localhost:8080/> を開く。
5. `Ctrl+C` で停止。

## フォルダー構成
```
demo/
├─ pom.xml                         ← 依存ライブラリ（≒ WEB-INF/lib の JAR）
└─ src/main/
   ├─ java/com/example/demo/
   │   ├─ DemoApplication.java     ← 起動クラス（main メソッド）
   │   ├─ controller/              ← コントローラ（≒ サーブレット）
   │   ├─ model/                   ← データクラス（JavaBeans）
   │   └─ service/                 ← ビジネスロジック
   ├─ resources/
   │   ├─ application.properties   ← 設定
   │   ├─ templates/               ← Thymeleaf のビュー
   │   └─ static/                  ← css / js / 画像
   └─ webapp/WEB-INF/jsp/          ← JSP のビュー（レッスン09）
```

## 考え方
- `@SpringBootApplication` は `com.example.demo` **とそのサブパッケージ** を読み込む。クラスは必ずその下に置く。
- Spring Boot は Tomcat を内蔵しているので、サーバーのインストールも `web.xml` も不要。

## 練習問題
`application.properties` に `server.port=8081` と書いて再起動し、ポートが変わったことを確認したら元に戻そう。
