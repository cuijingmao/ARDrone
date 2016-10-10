package com.parrot.freeflight.activities.task;


import com.parrot.freeflight.activities.picdemo.ColorType;

/**
 * Created by shisy13 on 16/8/24.
 */
public class TaskCommand {
    public String command;
    public   ColorType colorType;
    public   TaskMode taskMode;
    public static int n =7;                                //多存多少个最近值来进行pid控制
    public double[] centersX = new double[n];//保存最近n个点的x相对坐标
    public double[] centersY = new double[n]; //保存最近n个点的y坐标
    public double[] gaze = new double[n];    //gaze正表示上升，负表示下降
    public double[] pitch = new double[n];  //pitch正表示为后退，负值表示前进
    public double[] roll = new double[n];  //roll正表示右偏移，负值表示左偏移
    public double[] yaw = new double[n];    //yaw正代表右转弯，负左转表示左转弯
    public boolean  convertToSeekBall;     //是否由寻找路径模式转换为跟踪路径模式
    public static int radiusToKeep=250;           //小球半径像素值，跟踪小球时专用，控制四旋翼距离小球的远近，使得画面中小球大小保持这个值
    public long  runTime;
    public double pRatio;
    public double iRatio;
    public double dRatio;
    public TaskCommand() {
        this.command = "";
        this.colorType = ColorType.RED;
        this.taskMode = TaskMode.FOLLOWPATH;
        this.runTime=0;
        this.convertToSeekBall=false;
        this.iRatio = 0.1;
        this.pRatio = 0.1;
        this.dRatio = 0.3;
        for (int i = 0; i < this.n; i++) {
            this.centersX[i] = 0.0;
            this.centersY[i] = 0.0;
            this.gaze[i] = 0.0;
            this.pitch[i] = 0.0;
            this.roll[i] = 0.0;
            this.yaw[i] = 0.0;

        }
    }
    public TaskCommand cloneTo(TaskCommand tskCommand){
        tskCommand.runTime=this.runTime;
        tskCommand.taskMode=this.taskMode;
        tskCommand.colorType=this.colorType;
        tskCommand.command=this.command;
        tskCommand.convertToSeekBall=this.convertToSeekBall;
        tskCommand.centersX=this.centersX;
        tskCommand.centersY=this.centersY;
        tskCommand.gaze=this.gaze;
        tskCommand.pitch=this.pitch;
        tskCommand.roll=this.roll;
        tskCommand.yaw=this.yaw;
        tskCommand.pRatio=this.pRatio;
        tskCommand.iRatio=this.iRatio;
        tskCommand.dRatio=this.dRatio;
        return tskCommand;
    }
}
