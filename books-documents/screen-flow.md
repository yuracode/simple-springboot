# 書籍のメンテナンス（付録A）画面遷移図

4 画面のつながりを図にしたもの。URL は [url-design.md](url-design.md) の No を付けている。

- 実線の矢印（`-->`）：リンク（GET）で移動する
- 点線の矢印（`-.->`）：フォームを POST して、`redirect:` で移動する
- 付録Aには入力チェックがないので、POST のあとは必ず一覧へリダイレクトする
- 角の丸い箱はリクエスト（画面ではない）

## 1. 全体図

一覧（`/books`）が入口。ここから登録・詳細・編集に分かれる。

```mermaid
flowchart TD
    list["書籍一覧<br>GET /books（No.1）"]
    new["書籍の登録<br>GET /books/new（No.2）"]
    detail["書籍の詳細<br>GET /books/{id}（No.4）"]
    edit["書籍の編集<br>GET /books/{id}/edit（No.5）"]

    list --> new
    list --> detail
    list --> edit
```

## 2. 書籍の登録・編集・削除

```mermaid
flowchart TD
    list["書籍一覧<br>GET /books（No.1）"]
    new["書籍の登録<br>GET /books/new（No.2）"]
    edit["書籍の編集<br>GET /books/{id}/edit（No.5）"]

    list -->|新しく登録する| new
    list -->|編集| edit
    list -->|"検索・クリア（?keyword=...）"| list
    list -.->|"削除<br>POST /books/{id}/delete（No.7）"| list

    new -.->|"登録する<br>POST /books（No.3）"| list
    new -->|一覧へ戻る| list

    edit -.->|"更新する<br>POST /books/{id}（No.6）"| list
    edit -->|一覧へ戻る| list
```

- 削除はブラウザの確認ダイアログ（`confirm`）で「OK」を押したときだけ送信する。

---

## 3. 書籍の詳細

```mermaid
flowchart TD
    list["書籍一覧<br>GET /books（No.1）"]
    detail["書籍の詳細<br>GET /books/{id}（No.4）"]

    list -->|タイトル| detail
    detail -->|一覧へ戻る| list
```

---

## 4. 見つからないとき

URL の ID が DB にないときは、専用の画面を出さず **一覧へリダイレクトする**（GET から来たときは実線、POST から来たときは点線で書いている）。404 画面と `@ControllerAdvice` はレッスン18で扱う。

```mermaid
flowchart TD
    getReq(["GET の URL（No.4・5）<br>GET /books/{id}<br>GET /books/{id}/edit"])
    postReq(["POST の URL（No.6・7）<br>POST /books/{id}<br>POST /books/{id}/delete"])
    list["書籍一覧<br>GET /books（No.1）"]

    getReq -->|"ID がない<br>（メッセージなし）"| list
    postReq -.->|"ID がない<br>「その書籍は見つかりませんでした」"| list
```
