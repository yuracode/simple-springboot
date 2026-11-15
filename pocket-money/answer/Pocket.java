package com.example.demo.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

// お小遣い記録（pocket_entries テーブルの 1 行）。記録のフォームにも使う
@Data
public class Pocket {

    private Long id;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)   // "2026-10-05" を LocalDate で受け取る
    private LocalDate entryDate;

    private String type;          // "収入" か "支出"
    private Integer categoryId;

    // テーブルのカラムではない。JOIN で取った c.name AS category_name が入る
    private String categoryName;

    private Integer amount;
    private String memo;
}
