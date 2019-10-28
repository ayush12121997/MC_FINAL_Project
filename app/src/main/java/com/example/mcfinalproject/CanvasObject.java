package com.example.mcfinalproject;

import java.io.Serializable;

public class CanvasObject implements Serializable {
    private static final long serialVersionUID = 2L;
    float x;
    float y;
    int flag; // -1 down; 0 move; 1 up
    public CanvasObject(float x, float y, int flag)
    {
        this.x = x;
        this.y = y;
        this.flag = flag;
    }
}
