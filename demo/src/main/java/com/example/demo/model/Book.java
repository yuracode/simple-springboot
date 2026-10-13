package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// レッスン12：書籍（books テーブルの1行に対応）
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Integer id;
    private String title;
    private String author;
    private Integer price;
}
