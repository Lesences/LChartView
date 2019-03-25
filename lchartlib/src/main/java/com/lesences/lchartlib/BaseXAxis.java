package com.lesences.lchartlib;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import java.util.List;

/**
 * @author lesences  2018/1/22 12:30.
 */
class BaseXAxis extends View {
    private float mUnit;
    private ChartView mParent;

    private List<String> axisTexts;

    private BaseXAxis(Context context) {
        super(context);
    }

    BaseXAxis(Context context, ChartView mParent) {
        this(context);
        this.mParent = mParent;
    }

    public void setAxisTexts(List<String> axisTexts) {
        this.axisTexts = axisTexts;
        invalidate();
    }

    void setUnit(float mUnit) {
        this.mUnit = mUnit;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null == axisTexts || axisTexts.isEmpty()) {
            return;
        }
        int size = axisTexts.size();
        int curScorllX = getScrollX();
        int startIndex = Math.round(curScorllX / mUnit);
        if (startIndex < 0) {
            startIndex = 0;
        }
        int endIndex = startIndex + AttrsUtil.visibleCount;
        if (endIndex > size - 1) {
            endIndex = size - 1;
        }
        float centerY = getHeight() - mParent.axisPaint.getFontMetricsInt().descent;
        for (int i = startIndex; i <= endIndex; i++) {
            String itemStr = axisTexts.get(i);
            float centerX = getStartX(i);
            if (i == startIndex && centerX - curScorllX < mParent.getAxisToLeft() - AttrsUtil.curveWidth) {
                continue;
            }
            canvas.drawText(itemStr, centerX, centerY, mParent.axisPaint);
        }
    }

    private float getStartX(int index) {
        return mParent.getAxisToLeft() + index * mUnit;
    }
}
