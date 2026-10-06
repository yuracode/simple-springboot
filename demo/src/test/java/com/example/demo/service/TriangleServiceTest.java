package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.demo.model.Triangle;

class TriangleServiceTest {

    @Test
    void area() {
        Triangle t = new Triangle();
        t.setBase(10.0);
        t.setHeight(5.0);
        new TriangleService().calcArea(t);
        assertEquals(25.0, t.getArea());
    }
}
