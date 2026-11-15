package com.example.demo.model;

import lombok.Data;

// カテゴリ（categories テーブルの 1 行）。カテゴリのフォームにも使う
@Data
public class Category {

    private Long id;
    private String name;
    private Integer sortOrder;   // sort_order ⇔ sortOrder

    // テーブルのカラムではない。LEFT JOIN で数えた記録の件数（entry_count）が入る
    private Integer entryCount;
}
