package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.model.Triangle;

@Service
public class TriangleService {

    public void calcArea(Triangle t) {
        t.setArea(t.getBase() * t.getHeight() / 2);
    }
}
