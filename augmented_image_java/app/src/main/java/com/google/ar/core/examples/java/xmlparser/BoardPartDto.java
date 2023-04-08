package com.google.ar.core.examples.java.xmlparser;

public class BoardPartDto {
    float x;
    float z;
    String mpn;
    String device_package;

    public BoardPartDto() {
    }

    public BoardPartDto(float x, float z, String mpn, String device_package) {
        this.x = x;
        this.z = z;
        this.mpn = mpn;
        this.device_package = device_package;
    }

    @Override
    public String toString() {
        return "BoardDto{" +
                "x=" + x +
                ", y=" + z +
                ", mpn='" + mpn + '\'' +
                ", device_package='" + device_package + '\'' +
                '}';
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public String getMpn() {
        return mpn;
    }

    public String getDevice_package() {
        return device_package;
    }
}
