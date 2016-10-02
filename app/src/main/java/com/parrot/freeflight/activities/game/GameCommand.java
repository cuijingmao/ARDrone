package com.parrot.freeflight.activities.game;

import com.parrot.freeflight.activities.image.ColorType;

/**
 * Created by shisy13 on 16/8/24.
 */
public class GameCommand {
    public String command;
    public float gaz;
    public float roll;
    public float pitch;
    public float yaw;
    public ColorType colorType;
    public TaskMode taskMode;
     public double[] relativePosition= new double[2];
    public GameCommand(){
        this.command = "";
        this.gaz = 0;
        this.roll = 0;
        this.pitch = 0;
        this.yaw = 0;
        this.colorType=ColorType.RED;
        this.taskMode=TaskMode.FOLLOWPATH;
        this.relativePosition[0]=0.0;
        this.relativePosition[1]=0.0;

    }
}
