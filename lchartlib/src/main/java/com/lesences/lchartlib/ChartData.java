package com.lesences.lchartlib;

/**
 * @author lesences  2018/1/23 01:24.
 */
public class ChartData {
    private int index;
    private float value;
    private boolean empty;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
