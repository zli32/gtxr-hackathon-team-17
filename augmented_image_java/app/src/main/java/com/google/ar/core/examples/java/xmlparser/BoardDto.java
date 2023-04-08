package com.google.ar.core.examples.java.xmlparser;

public class BoardDto {
    float x;
    float y;
    String mpn;
    String device_package;

    @Override
    public String toString() {
        return "BoardDto{" +
                "x=" + x +
                ", y=" + y +
                ", mpn='" + mpn + '\'' +
                ", device_package='" + device_package + '\'' +
                '}';
    }
}
