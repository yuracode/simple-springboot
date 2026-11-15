package com.example.demo.model;

import lombok.Data;

// 月の集計の「カテゴリ別の支出」の 1 行（テーブルではなく、SQL の結果を入れる入れ物）
// SELECT c.name AS category, COUNT(*) AS count, SUM(e.amount) AS total
// の結果を PocketController の RowMapper で 1 行ずつ詰める
@Data
public class CategoryTotal {

    private String category;   // カテゴリ名
    private Integer count;     // 件数
    private Integer total;     // 合計金額
}
