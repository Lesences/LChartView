package com.lesences.lchartlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * @author lesences  2018/1/24 00:45.
 */
class BaseCircle extends View {
    private Paint radiusPaint;

    BaseCircle(Context context) {
        super(context);
        radiusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        radiusPaint.setStyle(Paint.Style.FILL);
    }

    void setRadiusColor(int radiusColor) {
        if (radiusColor != radiusPaint.getColor()) {
            radiusPaint.setColor(radiusColor);
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        radiusPaint.setAlpha((int) (255 * AttrsUtil.circleAlpha));
        canvas.drawCircle(AttrsUtil.maxRadius, AttrsUtil.maxRadius, AttrsUtil.maxRadius, radiusPaint);

        radiusPaint.setAlpha(255);
        canvas.drawCircle(AttrsUtil.maxRadius, AttrsUtil.maxRadius, AttrsUtil.midRadius, radiusPaint);
    }
}
