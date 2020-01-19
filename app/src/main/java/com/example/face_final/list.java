package com.example.face_final;

public class list {
    String s;
    float [][] input;


    public list(String s, float[][] input) {
        this.s = s;
        this.input = new float[0][128];
        this.input=input;
    }

    public String getS() {
        return s;
    }

    public float[][] getInput() {
        return input;
    }
}
