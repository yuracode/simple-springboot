# お小遣い帳（pocket-money）画面遷移図

`mock/` の 8 画面のつながりを図にしたもの。URL は [url-design.md](url-design.md) の No を付けている。

- 実線の矢印（`-->`）：リンク（GET）で移動する
- 点線の矢印（`-.->`）：フォームを POST して、成功したら `redirect:` で移動する
- 入力エラーのときは `redirect` せず、同じフォーム画面をエラー表示付きで返す
- 角の丸い箱はリクエスト（画面ではない）

## 1. 全体図

一覧（`/pocket`）が入口。ここから 3 つの機能に分かれる。

```mermaid
flowchart TD
    index["一覧・残高<br>GET /pocket（No.1）<br>mock/index.html"]
    new["記録の登録<br>GET /pocket/new（No.2）<br>mock/new.html"]
    edit["記録の編集<br>GET /pocket/{id}/edit（No.4）<br>mock/edit.html"]
    summary["月の集計<br>GET /pocket/summary（No.7）<br>mock/summary.html"]
    categories["カテゴリの一覧<br>GET /categories（No.8）<br>mock/categories.html"]
    catNew["カテゴリの追加<br>GET /categories/new（No.9）<br>mock/category-new.html"]
    catEdit["カテゴリの編集<br>GET /categories/{id}/edit（No.11）<br>mock/category-edit.html"]

    index --> new
    index --> edit
    index --> summary
    index --> categories
    categories --> catNew
    categories --> catEdit
```

## 2. 記録の登録・編集・削除

```mermaid
flowchart TD
    index["一覧・残高<br>GET /pocket（No.1）<br>mock/index.html"]
    new["記録の登録<br>GET /pocket/new（No.2）<br>mock/new.html"]
    edit["記録の編集<br>GET /pocket/{id}/edit（No.4）<br>mock/edit.html"]

    index -->|新しく記録する| new
    index -->|編集| edit
    index -->|"表示（?month=yyyy-MM）"| index
    index -.->|"削除<br>POST /pocket/{id}/delete（No.6）"| index

    new -.->|"登録する<br>POST /pocket（No.3）"| index
    new -->|一覧へ戻る| index

    edit -.->|"更新する<br>POST /pocket/{id}（No.5）"| index
    edit -.->|"この記録を削除する<br>POST /pocket/{id}/delete（No.6）"| index
    edit -->|一覧へ戻る| index
```

---

## 3. 月の集計

```mermaid
flowchart TD
    index["一覧・残高<br>GET /pocket（No.1）<br>mock/index.html"]
    summary["月の集計<br>GET /pocket/summary（No.7）<br>mock/summary.html"]

    index -->|月の集計を見る| summary
    summary -->|"表示・前の月・次の月<br>（?month=yyyy-MM）"| summary
    summary -->|一覧へ戻る| index
```

## 4. カテゴリの管理

```mermaid
flowchart TD
    index["一覧・残高<br>GET /pocket（No.1）<br>mock/index.html"]
    categories["カテゴリの一覧<br>GET /categories（No.8）<br>mock/categories.html"]
    catNew["カテゴリの追加<br>GET /categories/new（No.9）<br>mock/category-new.html"]
    catEdit["カテゴリの編集<br>GET /categories/{id}/edit（No.11）<br>mock/category-edit.html"]

    index -->|カテゴリの管理| categories
    categories -->|お小遣い帳へ戻る| index
    categories -->|カテゴリを追加する| catNew
    categories -->|編集| catEdit
    categories -.->|"削除<br>POST /categories/{id}/delete（No.13）"| categories

    catNew -.->|"登録する<br>POST /categories（No.10）"| categories
    catNew -->|カテゴリの一覧へ戻る| categories

    catEdit -.->|"更新する<br>POST /categories/{id}（No.12）"| categories
    catEdit -->|カテゴリの一覧へ戻る| categories
```

カテゴリが記録で使われているときは削除せずに `redirect:/categories` し、カテゴリの一覧に `errorMessage` を表示する。

---

## 5. 入力エラーのとき

登録・更新でチェックに引っかかったときは、リダイレクトせず同じフォームを返す（入力した値とエラーメッセージが表示される）。

```mermaid
flowchart LR
    new["記録の登録<br>mock/new.html"] -.->|"POST /pocket<br>エラー"| new
    edit["記録の編集<br>mock/edit.html"] -.->|"POST /pocket/{id}<br>エラー"| edit
    catNew["カテゴリの追加<br>mock/category-new.html"] -.->|"POST /categories<br>エラー"| catNew
    catEdit["カテゴリの編集<br>mock/category-edit.html"] -.->|"POST /categories/{id}<br>エラー"| catEdit
```

---

## 6. 見つからないとき（404）

URL の ID が DB にないときは、どの URL からでも「見つかりません」の画面をステータス 404 で返す（リダイレクトしない）。削除済みの記録の編集画面をブックマークから開いたときや、URL を直接打ったときに起きる。

```mermaid
flowchart TD
    pocketReq(["記録の URL（No.4〜6）<br>GET /pocket/{id}/edit<br>POST /pocket/{id}<br>POST /pocket/{id}/delete"])
    categoryReq(["カテゴリの URL（No.11〜13）<br>GET /categories/{id}/edit<br>POST /categories/{id}<br>POST /categories/{id}/delete"])
    notFound["見つかりません（404）<br>pocket/not-found.html<br>mock/not-found.html"]
    index["一覧・残高<br>GET /pocket（No.1）<br>mock/index.html"]
    categories["カテゴリの一覧<br>GET /categories（No.8）<br>mock/categories.html"]

    pocketReq -->|"ID がない<br>「記録が見つかりません」"| notFound
    categoryReq -->|"ID がない<br>「カテゴリが見つかりません」"| notFound
    notFound -->|お小遣い帳へ戻る| index
    notFound -->|カテゴリの管理| categories
```
