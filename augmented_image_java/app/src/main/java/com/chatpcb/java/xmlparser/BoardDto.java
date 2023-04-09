package com.chatpcb.java.xmlparser;

public class BoardDto {
    float width = 0;
    float height = 0;

    public BoardDto() {
    }

    public BoardDto(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "BoardDto{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
