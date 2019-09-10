package com.pudelko.model;

import java.util.Comparator;

public class Measurement implements Comparable<Measurement> {

    private int measurement_uid;
    private int test_uid;
    private double x;
    private double y;
    private double height;

    public int getMeasurement_uid() {
        return measurement_uid;
    }

    public void setMeasurement_uid(int measurement_uid) {
        this.measurement_uid = measurement_uid;
    }

    public int getTest_uid() {
        return test_uid;
    }

    public void setTest_uid(int test_uid) {
        this.test_uid = test_uid;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public int compareTo(Measurement m) {
        return Integer.compare(this.test_uid, m.test_uid);
    }
}
