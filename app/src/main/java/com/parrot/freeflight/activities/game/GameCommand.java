package com.parrot.freeflight.activities.game;

import com.parrot.freeflight.activities.image.ColorType;

/**
 * Created by shisy13 on 16/8/24.
 */
public class GameCommand {
    public String command;
    //    public float gaz;
//    public float roll;
//    public float pitch;
//    public float yaw;
    public ColorType colorType;
    public TaskMode taskMode;
    // public double[] relativePosition = new double[2];
    public static int n = 5;                                //多存多少个最近值来进行pid控制
    public double[] centersX = new double[n];//保存最近n个点的x相对坐标
    public double[] centersY = new double[n]; //保存最近n个点的y坐标
    public double[] gaze = new double[n];    //gaze正表示上升，负表示下降
    public double[] pitch = new double[n];  //pitch正表示为后退，负值表示前进
    public double[] roll = new double[n];  //roll正表示右偏移，负值表示左偏移
    public double[] yaw = new double[n];    //yaw正代表右转弯，负左转表示左转弯
    public double pRatio;
    public double iRatio;
    public double dRatio;

    public GameCommand() {
        this.command = "";
//        this.gaz = 0;
//        this.roll = 0;
//        this.pitch = 0;
//        this.yaw = 0;
        this.colorType = ColorType.RED;
        this.taskMode = TaskMode.FOLLOWPATH;
//        this.relativePosition[0] = 0.0;
//        this.relativePosition[1] = 0.0;
        this.iRatio = 0.1;
        this.pRatio = 0.1;
        this.dRatio = 0.1;
        for (int i = 0; i < this.n; i++) {
            this.centersX[i] = 0;
            this.centersY[i] = 0;
            this.gaze[i] = 0;
            this.pitch[i] = 0;
            this.roll[i] = 0;
            this.yaw[i] = 0;

            // this.centersX={0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0}; //保存最近10个点的x相对坐标

        }
    }
}
