# お小遣い帳（pocket-money）URL 設計書

## 1. URL 一覧

| No | メソッド | URL | 処理 | 画面（テンプレート） | モック |
|---|---|---|---|---|---|
| 1 | GET | `/pocket` | 一覧・残高の表示 | `pocket/list.html` | `mock/index.html` |
| 2 | GET | `/pocket/new` | 登録フォームの表示 | `pocket/form.html` | `mock/new.html` |
| 3 | POST | `/pocket` | 登録 | （成功）No.1 へリダイレクト<br>（エラー）`pocket/form.html` | ― |
| 4 | GET | `/pocket/{id}/edit` | 編集フォームの表示 | `pocket/form.html` | `mock/edit.html` |
| 5 | POST | `/pocket/{id}` | 更新 | （成功）No.1 へリダイレクト<br>（エラー）`pocket/form.html` | ― |
| 6 | POST | `/pocket/{id}/delete` | 削除 | No.1 へリダイレクト | ― |
| 7 | GET | `/pocket/summary` | 月の集計 | `pocket/summary.html` | `mock/summary.html` |
| 8 | GET | `/categories` | カテゴリの一覧 | `category/list.html` | `mock/categories.html` |
| 9 | GET | `/categories/new` | カテゴリの登録フォーム | `category/form.html` | `mock/category-new.html` |
| 10 | POST | `/categories` | カテゴリの登録 | （成功）No.8 へリダイレクト<br>（エラー）`category/form.html` | ― |
| 11 | GET | `/categories/{id}/edit` | カテゴリの編集フォーム | `category/form.html` | `mock/category-edit.html` |
| 12 | POST | `/categories/{id}` | カテゴリの更新 | （成功）No.8 へリダイレクト<br>（エラー）`category/form.html` | ― |
| 13 | POST | `/categories/{id}/delete` | カテゴリの削除 | No.8 へリダイレクト | ― |

- 登録（No.2）と編集（No.4）は同じテンプレート `form.html` を使い、見出しとボタン名・送信先だけを切り替える。
- POST のあとは必ず `redirect:` する（F5 での二重登録を防ぐ）。
- 更新と削除は HTML フォームから送るため、PUT・DELETE ではなく POST を使う。

## 2. 各 URL の詳細

### No.1 GET `/pocket` 一覧
| パラメータ | 種類 | 必須 | 説明 |
|---|---|---|---|
| `month` | クエリ | 任意 | `yyyy-MM` 形式（例：`2026-10`）。省略時は今月 |

画面に渡すもの（Model）

| 名前 | 内容 |
|---|---|
| `entries` | 指定月の記録（日付の昇順、同じ日付は ID 順） |
| `balance` | 残高（**全期間**の収入合計 − 支出合計） |
| `month` | 表示中の月 |
| `message` | 登録・更新・削除後のメッセージ（フラッシュ属性） |

### No.2 GET `/pocket/new` 登録フォーム
- 初期値：日付は今日、種類は「支出」

### No.3 POST `/pocket` 登録
送信する項目は「4. 入力項目と入力チェック」を参照。

| 結果 | 動き | メッセージ |
|---|---|---|
| 成功 | `redirect:/pocket` | 「○○ ○○円を登録しました」（カテゴリと金額） |
| 入力エラー | フォームを再表示し、入力値を残す | 各項目の下にエラー |

### No.4 GET `/pocket/{id}/edit` 編集フォーム
| パラメータ | 種類 | 説明 |
|---|---|---|
| `id` | パス | 記録の ID |

- 存在しない ID のときは 404（「5. エラー」を参照）

### No.5 POST `/pocket/{id}` 更新
| 結果 | 動き | メッセージ |
|---|---|---|
| 成功 | `redirect:/pocket` | 「○○ ○○円に更新しました」 |
| 入力エラー | フォームを再表示し、入力値を残す | 各項目の下にエラー |
| ID が存在しない | 404 | ― |

### No.6 POST `/pocket/{id}/delete` 削除
| 結果 | 動き | メッセージ |
|---|---|---|
| 成功 | `redirect:/pocket` | 「○○ ○○円を削除しました」 |
| ID が存在しない | 404 | ― |

### No.7 GET `/pocket/summary` 月の集計
| パラメータ | 種類 | 必須 | 説明 |
|---|---|---|---|
| `month` | クエリ | 任意 | `yyyy-MM` 形式。省略時は今月 |

画面に渡すもの（Model）

| 名前 | 内容 |
|---|---|
| `month` | 表示中の月 |
| `incomeTotal` | その月の収入合計 |
| `expenseTotal` | その月の支出合計 |
| `difference` | 収入合計 − 支出合計 |
| `categoryTotals` | カテゴリ別の支出（カテゴリ・件数・合計）、合計の多い順 |
| `prevMonth` / `nextMonth` | 前の月・次の月（リンク用） |

### No.8 GET `/categories` カテゴリの一覧
画面に渡すもの（Model）

| 名前 | 内容 |
|---|---|
| `categories` | すべてのカテゴリ（表示順 `sort_order`、同じなら ID 順）。各カテゴリの記録の件数も持たせる |
| `message` | 登録・更新・削除後のメッセージ（フラッシュ属性） |
| `errorMessage` | 削除できなかったときのメッセージ（フラッシュ属性） |

### No.9 GET `/categories/new` カテゴリの登録フォーム
- 初期値：表示順は「今ある表示順の最大値 + 1」

### No.10 POST `/categories` カテゴリの登録
| 結果 | 動き | メッセージ |
|---|---|---|
| 成功 | `redirect:/categories` | 「○○を登録しました」 |
| 入力エラー・名前の重複 | フォームを再表示し、入力値を残す | 各項目の下にエラー |

### No.11 GET `/categories/{id}/edit` カテゴリの編集フォーム
- 存在しない ID のときは 404

### No.12 POST `/categories/{id}` カテゴリの更新
| 結果 | 動き | メッセージ |
|---|---|---|
| 成功 | `redirect:/categories` | 「○○に更新しました」 |
| 入力エラー・名前の重複 | フォームを再表示し、入力値を残す | 各項目の下にエラー |
| ID が存在しない | 404 | ― |

- 名前の重複チェックでは、自分自身（同じ ID）は除く（名前を変えずに表示順だけ変えられるように）。

### No.13 POST `/categories/{id}/delete` カテゴリの削除
| 結果 | 動き | メッセージ |
|---|---|---|
| 成功 | `redirect:/categories` | 「○○を削除しました」 |
| 記録で使われている | `redirect:/categories` | 「○○は N 件の記録で使われているため削除できません」（`errorMessage`） |
| ID が存在しない | 404 | ― |

- 削除の前に `SELECT COUNT(*) FROM pocket_entries WHERE category_id = ?` で件数を調べる。外部キー制約があるので、調べずに消そうとすると DB がエラー（`DataIntegrityViolationException`）を返す。

## 3. テーブル

`categories`（カテゴリ）と `pocket_entries`（お小遣い記録）の 2 つ。詳細は `table-design.md` を参照。

## 4. 入力項目と入力チェック

| 項目 | name | 入力欄 | チェック | エラーメッセージ |
|---|---|---|---|---|
| 日付 | `entryDate` | `<input type="date">` | 必須 | 日付を入力してください |
| 種類 | `type` | ラジオボタン（収入・支出） | 必須 | 種類を選んでください |
| カテゴリ | `categoryId` | セレクトボックス（値はカテゴリ ID） | 必須 | カテゴリを選んでください |
| 金額 | `amount` | テキスト | 必須、1〜1,000,000 | 金額は1円以上で入力してください |
| メモ | `memo` | テキスト | 100文字以内 | メモは100文字以内で入力してください |

- カテゴリの選択肢は `categories` テーブルから表示順（`sort_order`）で取得する（おこづかい・お手伝い・おやつ・本・文房具・あそび・その他）
- 日付には `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` を付けて `LocalDate` で受け取る。

### カテゴリのフォーム（No.9〜12）

| 項目 | name | 入力欄 | チェック | エラーメッセージ |
|---|---|---|---|---|
| カテゴリ名 | `name` | テキスト | 必須、30文字以内、重複不可 | カテゴリ名を入力してください／カテゴリ名は30文字以内で入力してください／そのカテゴリ名はすでに登録されています |
| 表示順 | `sortOrder` | テキスト | 必須、0〜999 | 表示順は0〜999で入力してください |

- 重複チェックは Bean Validation ではできないので、コントローラ（またはサービス）で DB を調べ、`BindingResult.rejectValue("name", ...)` でエラーを追加する。

## 5. エラー

| 状況 | ステータス | 画面 |
|---|---|---|
| 存在しない ID（No.4〜6） | 404 | `pocket/not-found.html`「記録が見つかりません（ID：N）」（モック：`mock/not-found.html`） |
| 存在しない ID（No.11〜13） | 404 | `pocket/not-found.html`「カテゴリが見つかりません（ID：N）」 |
| `month` の形式が不正 | 400 | 今月の表示に戻す（またはエラー画面） |
