package com.example.demo.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// レッスン10：三角形（底辺・高さ・面積）
@Data
public class Triangle {
    @NotNull(message = "底辺を入力してください")
    @Positive(message = "底辺は0より大きい値を入力してください")
    private Double base;

    @NotNull(message = "高さを入力してください")
    @Positive(message = "高さは0より大きい値を入力してください")
    private Double height;

    private Double area;
}
