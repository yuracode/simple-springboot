# 書籍のメンテナンス（付録A）URL 設計書

## 1. URL 一覧

| No | メソッド | URL | 処理 | 画面（テンプレート） | モック |
|---|---|---|---|---|---|
| 1 | GET | `/books` | 一覧の表示（キーワード検索つき） | `books.html` | `mock/books.html` |
| 2 | GET | `/books/new` | 登録フォームの表示 | `book-form.html` | `mock/book-new.html` |
| 3 | POST | `/books` | 登録 | No.1 へリダイレクト | ― |
| 4 | GET | `/books/{id}` | 詳細の表示 | `book-detail.html` | `mock/book-detail.html` |
| 5 | GET | `/books/{id}/edit` | 編集フォームの表示 | `book-form.html` | `mock/book-edit.html` |
| 6 | POST | `/books/{id}` | 更新 | No.1 へリダイレクト | ― |
| 7 | POST | `/books/{id}/delete` | 削除 | No.1 へリダイレクト | ― |

- 登録（No.2）と編集（No.5）は同じテンプレート `book-form.html` を使い、`book.id` が `null` かどうかで見出し・ボタン名・送信先を切り替える。
- POST のあとは必ず `redirect:` する（PRG パターン。F5 での二重登録を防ぐ）。
- 更新と削除は HTML フォームから送るため、PUT・DELETE ではなく POST を使う。
- `/books/new` と `/books/{id}` は、Spring が固定の URL を優先するので書く順番を気にしなくてよい。
- コントローラ（`BookController`）は **リポジトリ（`BookRepository`）を直接呼ぶ**。サービス層は付録Bで扱う。

## 2. 各 URL の詳細

### No.1 GET `/books` 一覧
| パラメータ | 種類 | 必須 | 説明 |
|---|---|---|---|
| `keyword` | クエリ | 任意 | タイトルか著者に含まれる文字。空または省略時は全件（前後の空白は除く） |

画面に渡すもの（Model）

| 名前 | 内容 |
|---|---|
| `books` | 書籍の一覧（ID 順） |
| `keyword` | 入力されたキーワード（検索欄に残すため） |
| `message` | 登録・更新・削除後のメッセージ（フラッシュ属性） |

### No.2 GET `/books/new` 登録フォーム
- 空の `Book`（`id` は `null`）を `book` として渡す。

### No.3 POST `/books` 登録
送信する項目は「4. 入力項目」を参照。

| 結果 | 動き | メッセージ |
|---|---|---|
| 成功 | `redirect:/books` | 「○○」を登録しました（タイトル） |

- `id` は `SERIAL` なので DB が自動で振る。

### No.4 GET `/books/{id}` 詳細
| パラメータ | 種類 | 説明 |
|---|---|---|
| `id` | パス | 書籍の ID |

| 結果 | 動き |
|---|---|
| 見つかった | `book-detail.html` を表示（Model：`book`） |
| ID が存在しない | `redirect:/books`（メッセージなし） |

### No.5 GET `/books/{id}/edit` 編集フォーム
| 結果 | 動き |
|---|---|
| 見つかった | `book-form.html` を今の値が入った状態で表示（Model：`book`） |
| ID が存在しない | `redirect:/books`（メッセージなし） |

### No.6 POST `/books/{id}` 更新
| 結果 | 動き | メッセージ |
|---|---|---|
| 成功（更新行数 1） | `redirect:/books` | 「○○」を更新しました |
| ID が存在しない（更新行数 0） | `redirect:/books` | その書籍は見つかりませんでした |

- どの本を更新するかは **URL の id** で決める（`book.setId(id)`）。フォームから送られた値をそのまま信じない。

### No.7 POST `/books/{id}/delete` 削除
| 結果 | 動き | メッセージ |
|---|---|---|
| 成功（削除行数 1） | `redirect:/books` | ID N の書籍を削除しました |
| ID が存在しない（削除行数 0） | `redirect:/books` | その書籍は見つかりませんでした |

## 3. テーブル

`books`（書籍）の 1 つ。詳細は `table-design.md` を参照。

## 4. 入力項目

| 項目 | name | 入力欄 | チェック |
|---|---|---|---|
| タイトル | `title` | テキスト（`size="40"`） | なし |
| 著者 | `author` | テキスト | なし |
| 価格 | `price` | `<input type="number" min="0">` ＋「円」 | なし |

- `@ModelAttribute Book book` でフォームの値を `Book` に詰める（フォーム専用クラス `BookForm` はレッスン17）。
- 入力チェックは付録Aではまだ行わない（レッスン18）。価格を空にすると DB の `NOT NULL` 違反でエラー画面になる。

## 5. エラー

| 状況 | ステータス | 画面 |
|---|---|---|
| 存在しない ID（No.4・5） | 302 | 一覧へリダイレクト（メッセージなし） |
| 存在しない ID（No.6・7） | 302 | 一覧へリダイレクト「その書籍は見つかりませんでした」 |
| 価格が空で登録・更新 | 500 | Spring Boot のエラー画面（`NOT NULL` 違反） |
