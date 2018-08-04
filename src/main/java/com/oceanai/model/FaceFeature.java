package com.oceanai.model;

/**
 * Created by Wangke on 2017/11/15.
 */
public class FaceFeature {
    //private float[] feature;
    private int left;
    private int top;
    private int width;
    private int height;
    private LandMark landmark;
    private double blur;
    private double quality;
    private double score;
    private double sideFace;

    public FaceFeature(float[] feature, int left, int top, int width, int height){
        //this.feature = feature;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public FaceFeature(float[] feature, int left, int top, int width, int height, LandMark landMark) {
        //this.feature = feature;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.landmark = landMark;
    }

   /* public float[] getFeature() {
        return feature;
    }

    public void setFeature(float[] feature) {
        this.feature = feature;
    }*/

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public LandMark getLandmark() {
        return landmark;
    }

    public void setLandmark(LandMark landmark) {
        this.landmark = landmark;
    }

    public double getBlur() {
        return blur;
    }

    public void setBlur(double blur) {
        this.blur = blur;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getSideFace() {
        return sideFace;
    }

    public void setSideFace(double sideFace) {
        this.sideFace = sideFace;
    }
}
