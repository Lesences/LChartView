package com.lesences.lchartlib;

import android.graphics.RectF;

/**
 * @author lesences  2018/1/31 11:56.
 */
class ClickData {
    private RectF rectF;
    private int curveNO;

    ClickData(RectF rectF, int curveNO) {
        this.rectF = rectF;
        this.curveNO = curveNO;
    }

    void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    RectF getRectF() {
        return rectF;
    }

    void setCurveNO(int curveNO) {
        this.curveNO = curveNO;
    }

    int getCurveNO() {
        return curveNO;
    }
}
