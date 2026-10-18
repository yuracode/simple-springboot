package com.example.demo.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// レッスン17：登録フォームの入力値（エンティティの Book とは分ける）
// レッスン18：入力チェックを付ける
@Data
public class BookForm {

    @NotBlank(message = "タイトルを入力してください")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    private String title;

    @NotBlank(message = "著者を入力してください")
    @Size(max = 50, message = "著者は50文字以内で入力してください")
    private String author;

    @NotNull(message = "価格を入力してください")
    @Min(value = 0, message = "価格は0円以上で入力してください")
    @Max(value = 100000, message = "価格は100,000円以下で入力してください")
    private Integer price;
}
