package com.oceanai.model;

/**
 * Created by WangRupeng on 2017/12/8.
 */

/**
 * x:[eye_x, , , , ,]
 * y:[eye_y, , , , ,]
 */
public class LandMark {
    private int[] x;
    private int[] y;

    public LandMark(int[] x, int y[]) {
        this.x = x;
        this.y = y;
    }

    public int[] getX() {
        return x;
    }

    public void setX(int[] x) {
        this.x = x;
    }

    public int[] getY() {
        return y;
    }

    public void setY(int[] y) {
        this.y = y;
    }

    public int getDistance() {
        int distance = (x[1] - x[0])*(x[1] - x[0]) + (y[1] - y[0])*(y[1] - y[0]);
        int dis = (int)Math.sqrt(distance);
        return dis;
    }

    public boolean isSizeUp(int distance) {
        return (getDistance() - distance) > 0 ? true : false;
    }

}
