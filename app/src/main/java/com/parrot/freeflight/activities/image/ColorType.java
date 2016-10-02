package com.parrot.freeflight.activities.image;

import com.parrot.freeflight.utils.StreamUtils;

/**
 * Created by Administrator on 2016/9/6.
 */
public enum ColorType {
    RED("红", 0), YELLOW("黄", 1), BLUE("蓝", 2), GREEN("绿", 3);
    private int value = 0;
    private String name = "红";

    ColorType(String name, int value) {
        this.value = value;
        this.name = name;
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
