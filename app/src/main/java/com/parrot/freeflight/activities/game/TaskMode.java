package com.parrot.freeflight.activities.game;

/**
 * Created by Administrator on 2016/9/6.
 */
public enum TaskMode {
    FOLLOWPATH("跟踪路径模式", 0), TRACKBALL("跟踪小球模式", 1);
    int value = 0;
    String name = "跟踪路径模式";

    TaskMode(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
