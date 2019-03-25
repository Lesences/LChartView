package com.lesences.lchartlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.View;

/**
 * @author lesences  2018/1/24 00:11.
 */
class BaseBubble extends View {
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path bubblePath = new Path();
    private Rect rect = new Rect();
    private Rect bounds = new Rect();

    private String text = null;

    BaseBubble(Context context) {
        super(context);
        textPaint.setTextSize(AttrsUtil.bubbleTextSize);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        bubblePaint.setStrokeWidth(AttrsUtil.bubbleLineWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bubblePath.reset();
        int halfLine = (int) (bubblePaint.getStrokeWidth() * 0.5f + 0.5f);
        rect = new Rect(halfLine, halfLine, w - halfLine, h - halfLine);
        int bubbleAnchorX = rect.left + rect.width() / 2;
        int bubbleAnchorY = rect.top + rect.height();

        float arrowWidth = AttrsUtil.bubbleArrowWidth;
        float arrowHeight = AttrsUtil.bubbleArrowHeight;
        float radius = AttrsUtil.bubbleRadius;

        bubblePath.moveTo(bubbleAnchorX, bubbleAnchorY);
        bubblePath.lineTo(rect.left + rect.width() / 2 + arrowWidth / 2, rect.top + rect.height() - arrowHeight);

        // Go to bottom-right
        bubblePath.lineTo(rect.left + rect.width() - radius, rect.top + rect.height() - arrowHeight);

        // Bottom-right arc
        bubblePath.arcTo(new RectF(rect.left + rect.width() - 2 * radius, rect.top + rect.height() - arrowHeight - 2 * radius, rect.left + rect.width(), rect.top + rect.height() - arrowHeight), 90, -90);

        // Go to upper-right
        bubblePath.lineTo(rect.left + rect.width(), rect.top + arrowHeight);

        // Upper-right arc
        bubblePath.arcTo(new RectF(rect.left + rect.width() - 2 * radius, rect.top, rect.right, rect.top + 2 * radius), 0, -90);

        // Go to upper-left
        bubblePath.lineTo(rect.left + radius, rect.top);

        // Upper-left arc
        bubblePath.arcTo(new RectF(rect.left, rect.top, rect.left + 2 * radius, rect.top + 2 * radius), 270, -90);

        // Go to bottom-left
        bubblePath.lineTo(rect.left, rect.top + rect.height() - arrowHeight - radius);

        // Bottom-left arc
        bubblePath.arcTo(new RectF(rect.left, rect.top + rect.height() - arrowHeight - 2 * radius, rect.left + 2 * radius, rect.top + rect.height() - arrowHeight), 180, -90);

        bubblePath.lineTo(rect.left + rect.width() / 2 - arrowWidth / 2, rect.top + rect.height() - arrowHeight);

        bubblePath.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        bubblePaint.setColor(AttrsUtil.bubbleBgColor);
        bubblePaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(bubblePath, bubblePaint);

        textPaint.getTextBounds(text, 0, text.length(), bounds);
        float startX = rect.centerX();
        float startY = rect.height() + bounds.centerY() - textPaint.getFontMetricsInt().descent;
        canvas.drawText(text, startX, startY, textPaint);


        bubblePaint.setColor(AttrsUtil.bubbleBorderColor);
        bubblePaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(bubblePath, bubblePaint);
    }

    void setTextThing(int textColor, String text) {
        textPaint.setColor(textColor);
        this.text = text;
        postInvalidate();
    }
}
