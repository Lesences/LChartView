package com.lesences.lchartlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;


import java.text.DecimalFormat;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * @author lesences  2018/1/22 11:55.
 */
public class ChartView extends FrameLayout {
    private final static String TAG = ChartView.class.getSimpleName();

    private final static int ANIMATOR_DURATION = 2000;
    private final static int VISIBLE_COUNT = 5;
    private final static int AXIS_TEXT_SIZE = 10;
    private final static float LINE_WIDTH = 0.5f;
    private final static float LIMIT_DASH_GAP = 2f;
    private final static float LIMIT_DASH_WIDTH = 3.5f;
    private final static float SHADE_TOP_ALPHA = 0.43f;
    private final static float SHADE_BOTTOM_ALPHA = 0.12f;
    private final static String AXIS_TEXT = "1雾00";

    private Context mContext;
    private BaseChart mChart;
    private BaseXAxis mXAxis;
    private BaseBubble mBubble;
    private BaseCircle mCircle;

    Rect textBounds = new Rect();
    Paint axisPaint;
    Paint linePaint;
    Paint curvePaint;

    private Path limitPath = new Path();
    private String maxAxisText = AXIS_TEXT;

    int topMargin;
    private int bottomMargin;
    private int yAxisWidth;
    private DashPathEffect limitEffect;
    private float yAxisMax;
    private float yAxisMin;
    private float[] limitValues;
    private DecimalFormat valueFormat;

    private List<List<ChartData>> chartDataLists = null;
    private int[] curveColors = null;

    private float clickToStart = -1f;
    private int clickCurveLine = -1;

    public ChartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ChartView);
        initAttrs(ta);

        initPaints();
        initChilds();
    }

    private void initPaints() {
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(AttrsUtil.axisTextColor);
        axisPaint.setTextAlign(Paint.Align.CENTER);
        axisPaint.setTextSize(AttrsUtil.axisTextSize);
        axisPaint.setAntiAlias(true);


        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(AttrsUtil.borderColor);
        linePaint.setStrokeWidth(AttrsUtil.lineWidth);
        linePaint.setAntiAlias(true);

        curvePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        curvePaint.setAntiAlias(true);
        curvePaint.setStrokeWidth(AttrsUtil.curveWidth);

        axisPaint.getTextBounds(maxAxisText, 0, maxAxisText.length(), textBounds);
    }

    private void initAttrs(TypedArray ta) {
        AttrsUtil.axisTextColor = ta.getColor(R.styleable.ChartView_cv_axisTextColor, Color.LTGRAY);
        AttrsUtil.borderColor = ta.getColor(R.styleable.ChartView_cv_borderColor, Color.LTGRAY);
        AttrsUtil.limitColor = ta.getColor(R.styleable.ChartView_cv_limitColor, Color.LTGRAY);
        AttrsUtil.bubbleBgColor = ta.getColor(R.styleable.ChartView_cv_bubbleBgColor, Color.WHITE);
        AttrsUtil.bubbleBorderColor = ta.getColor(R.styleable.ChartView_cv_bubbleBorderColor, Color.LTGRAY);
        AttrsUtil.verticalColor = ta.getColor(R.styleable.ChartView_cv_verticalColor, Color.LTGRAY);

        AttrsUtil.axisTextSize = ta.getDimensionPixelSize(R.styleable.ChartView_cv_axisTextSize, Util.dip2px(AXIS_TEXT_SIZE));
        AttrsUtil.xaAxisToTop = ta.getDimensionPixelSize(R.styleable.ChartView_cv_xaAxisToTop, Util.dip2px(0f));
        AttrsUtil.chartToRight = ta.getDimensionPixelSize(R.styleable.ChartView_cv_chartToRight, Util.dip2px(1.5f));
        AttrsUtil.clickRadius = ta.getDimensionPixelSize(R.styleable.ChartView_cv_clickRadius, Util.dip2px(5f));
        AttrsUtil.curveWidth = ta.getDimensionPixelSize(R.styleable.ChartView_cv_curveWidth, Util.dip2px(LINE_WIDTH));
        AttrsUtil.lineWidth = ta.getDimensionPixelSize(R.styleable.ChartView_cv_lineWidth, Util.dip2px(LINE_WIDTH));
        AttrsUtil.limitDashGap = ta.getDimensionPixelSize(R.styleable.ChartView_cv_limitDashGap, Util.dip2px(LIMIT_DASH_GAP));
        AttrsUtil.limitDashWidth = ta.getDimensionPixelSize(R.styleable.ChartView_cv_limitDashWidth, Util.dip2px(LIMIT_DASH_WIDTH));
        AttrsUtil.maxRadius = ta.getDimensionPixelSize(R.styleable.ChartView_cv_maxRadius, Util.dip2px(4f));
        AttrsUtil.midRadius = ta.getDimensionPixelSize(R.styleable.ChartView_cv_midRadius, Util.dip2px(2.5f));
        AttrsUtil.minRadius = ta.getDimensionPixelSize(R.styleable.ChartView_cv_minRadius, Util.dip2px(1.5f));

        AttrsUtil.bubbleArrowHeight = ta.getDimensionPixelSize(R.styleable.ChartView_cv_bubbleArrowHeight, Util.dip2px(3f));
        AttrsUtil.bubbleArrowWidth = ta.getDimensionPixelSize(R.styleable.ChartView_cv_bubbleArrowWidth, Util.dip2px(5f));
        AttrsUtil.bubbleGap = ta.getDimensionPixelSize(R.styleable.ChartView_cv_bubbleGap, Util.dip2px(2f));
        AttrsUtil.bubbleHeight = ta.getDimensionPixelSize(R.styleable.ChartView_cv_bubbleHeight, Util.dip2px(16f));
        AttrsUtil.bubbleWidth = ta.getDimensionPixelSize(R.styleable.ChartView_cv_bubbleWidth, Util.dip2px(20f));
        AttrsUtil.bubbleRadius = ta.getDimensionPixelSize(R.styleable.ChartView_cv_bubbleRadius, Util.dip2px(8f));
        AttrsUtil.bubbleTextSize = ta.getDimensionPixelSize(R.styleable.ChartView_cv_bubbleTextSize, Util.dip2px(11f));
        AttrsUtil.bubbleLineWidth = ta.getDimensionPixelSize(R.styleable.ChartView_cv_bubbleLineWidth, Util.dip2px(LINE_WIDTH));


        AttrsUtil.shaderTopAlpha = ta.getFloat(R.styleable.ChartView_cv_shadeTopAlpha, SHADE_TOP_ALPHA);
        AttrsUtil.shaderBottomAlpha = ta.getFloat(R.styleable.ChartView_cv_shadeBottomAlpha, SHADE_BOTTOM_ALPHA);
        AttrsUtil.circleAlpha = ta.getFloat(R.styleable.ChartView_cv_circleAlpha, SHADE_BOTTOM_ALPHA);

        int timeInterpolator = ta.getInt(R.styleable.ChartView_cv_timeInterpolator, 0);
        switch (timeInterpolator) {
            default:
            case 0:
                AttrsUtil.interpolator = new AccelerateInterpolator();
                break;
            case 1:
                AttrsUtil.interpolator = new DecelerateInterpolator();
                break;
            case 2:
                AttrsUtil.interpolator = new AccelerateDecelerateInterpolator();
                break;
        }

        AttrsUtil.visibleCount = ta.getInteger(R.styleable.ChartView_cv_visibleCount, VISIBLE_COUNT);
        AttrsUtil.animatorDuration = ta.getInteger(R.styleable.ChartView_cv_animatorDuration, ANIMATOR_DURATION);
        maxAxisText = ta.getString(R.styleable.ChartView_cv_yAxisMaxText);
        if (null == maxAxisText) {
            maxAxisText = AXIS_TEXT;
        }

        String formatStr = ta.getString(R.styleable.ChartView_cv_valueFormat);
        if (TextUtils.isEmpty(formatStr)) {
            valueFormat = new DecimalFormat();
        } else {
            valueFormat = new DecimalFormat(formatStr);
        }
        ta.recycle();
    }

    private void initChilds() {
        yAxisWidth = textBounds.width();
        int textHeight = textBounds.height();
        topMargin = AttrsUtil.bubbleArrowHeight + AttrsUtil.maxRadius + AttrsUtil.bubbleHeight
                + AttrsUtil.bubbleGap + AttrsUtil.bubbleLineWidth * 2;
        bottomMargin = 3 * textHeight / 2 + AttrsUtil.xaAxisToTop;

        //xAxis
        mXAxis = new BaseXAxis(mContext, this);
        LayoutParams xAxisLp = new LayoutParams(MATCH_PARENT, textHeight);
        xAxisLp.gravity = Gravity.BOTTOM;
        addView(mXAxis, xAxisLp);

        //chart
        mChart = new BaseChart(mContext, this);
        LayoutParams chartLp = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        chartLp.gravity = Gravity.TOP | Gravity.END;
        chartLp.topMargin = topMargin - AttrsUtil.minRadius - AttrsUtil.lineWidth;
        chartLp.leftMargin = yAxisWidth;
        chartLp.rightMargin = AttrsUtil.chartToRight;
        chartLp.bottomMargin = bottomMargin - AttrsUtil.minRadius;
        addView(mChart, chartLp);

        //bubble
        mBubble = new BaseBubble(mContext);
        LayoutParams mBubbleLp = new LayoutParams(AttrsUtil.bubbleWidth,
                AttrsUtil.bubbleArrowHeight + AttrsUtil.bubbleHeight);
        addView(mBubble, mBubbleLp);

        //circle
        mCircle = new BaseCircle(mContext);
        int maxRadius2 = AttrsUtil.maxRadius * 2;
        LayoutParams mCircleLp = new LayoutParams(maxRadius2, maxRadius2);
        addView(mCircle, mCircleLp);

        setBubbleCircle(INVISIBLE);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (yAxisMax <= yAxisMin) {
            super.dispatchDraw(canvas);
            return;
        }
        int lineRight = getWidth();
        drawLimits(canvas, lineRight);

        drawClickLine(canvas);

        drawYAxisTexts(canvas, yAxisMax, yAxisMin);
        drawYAxisTexts(canvas, limitValues);

        //绘制上下边界
        float startX = yAxisWidth + AttrsUtil.minRadius;
        int toBottom = (int) getChartBottom();
        linePaint.setColor(AttrsUtil.borderColor);
        linePaint.setPathEffect(null);
        canvas.drawLines(new float[]{
                startX, topMargin, lineRight, topMargin,
                startX, toBottom, lineRight, toBottom
        }, linePaint);

        super.dispatchDraw(canvas);
    }

    /**
     * 绘制竖直选中提示线
     */
    private void drawClickLine(Canvas canvas) {
        if (clickToStart == -1f || clickCurveLine == -1) {
            return;
        }
        float startX = yAxisWidth + clickToStart;
        linePaint.setColor(AttrsUtil.verticalColor);
        limitPath.reset();
        limitPath.moveTo(startX, topMargin);
        limitPath.lineTo(startX, getChartBottom());
        linePaint.setPathEffect(getPathEffect());
        canvas.drawPath(limitPath, linePaint);
    }

    /**
     * 绘制竖(Y)轴坐标
     */
    private void drawYAxisTexts(Canvas canvas, float... values) {
        if (null == values) {
            return;
        }
        for (float value : values) {
            float valueHeight = getValueToTop(value);
            String valueStr = getValueString(value);
            axisPaint.getTextBounds(valueStr, 0, valueStr.length(), textBounds);
            canvas.drawText(valueStr, yAxisWidth - textBounds.centerX(),
                    valueHeight - textBounds.centerY(), axisPaint);
        }
    }

    /**
     * 绘制提示线
     */
    private void drawLimits(Canvas canvas, int lineRight) {
        if (null == limitValues) {
            return;
        }
        float startX = yAxisWidth + AttrsUtil.minRadius;
        for (float limit : limitValues) {
            float limitY = getValueToTop(limit);
            linePaint.setColor(AttrsUtil.limitColor);
            limitPath.reset();
            limitPath.moveTo(startX, limitY);
            limitPath.lineTo(lineRight, limitY);
            linePaint.setPathEffect(getPathEffect());
            canvas.drawPath(limitPath, linePaint);
        }
    }

    private DashPathEffect getPathEffect() {
        if (null == limitEffect) {
            limitEffect = new DashPathEffect(
                    new float[]{AttrsUtil.limitDashWidth, AttrsUtil.limitDashGap}, 0);
        }
        return limitEffect;
    }

    private void setBubbleCircle(int visibility) {
        mBubble.setVisibility(visibility);
        mCircle.setVisibility(visibility);
    }

    float getValueToTop(float yValue) {
        float tempHeight = getChartBottom() - topMargin;
        if (yAxisMax <= yAxisMin || yValue > yAxisMax) {
            return tempHeight;
        }
        float yUnit = tempHeight / (yAxisMax - yAxisMin);
        return topMargin + yUnit * (yAxisMax - yValue) - AttrsUtil.minRadius;
    }

    float getChartBottom() {
        return getHeight() - bottomMargin;
    }

    float getAxisToLeft() {
        return yAxisWidth + AttrsUtil.minRadius;
    }

    String getValueString(float value) {
        return valueFormat.format(value);
    }

    void onChartScrollX(int scrollX, float unit) {
        mXAxis.setUnit(unit);
        mXAxis.scrollTo(scrollX, 0);
    }

    void clickEvent(ClickData clickData) {
        boolean isEmpty = null == clickData;
        this.clickCurveLine = isEmpty ? -1 : clickData.getCurveNO();
        this.clickToStart = isEmpty ? -1.f : clickData.getRectF().centerX() - mChart.getScrollX();
        postInvalidate();
    }

    private ChartData clickStartToChartData(float chartScrollX) {
        int index = Math.round((chartScrollX - AttrsUtil.minRadius) / mChart.mUnit);
        List<ChartData> itemList = chartDataLists.get(clickCurveLine);
        int itemListSize = itemList.size();
        if (index < 0) {
            index = 0;
        } else if (index > itemListSize - 1) {
            index = itemListSize - 1;
        }
        return itemList.get(index);
    }

    /**
     * 此处作为滑动结束，并且手指离开屏幕的回调
     *
     * @param isFinished true: 结束滑动显示,false 开始滑动/或正在滑动
     */
    void scrollStatu(boolean isFinished, float chartScrollX) {
        if (null == curveColors || null == chartDataLists) {
            return;
        }
        if (clickCurveLine == -1 || clickToStart == -1f || !isFinished) {
            setBubbleCircle(INVISIBLE);
        } else {
            ChartData chartData = clickStartToChartData(clickToStart + chartScrollX);
            if (null == chartData || chartData.isEmpty()) {
                setBubbleCircle(INVISIBLE);
                return;
            }
            float startX = yAxisWidth + clickToStart;
            float value = chartData.getValue();
            float startY = getValueToTop(value);
            int color = curveColors[clickCurveLine];
            mBubble.setX(startX - AttrsUtil.bubbleWidth * 0.5f);
            float bubbleY = AttrsUtil.bubbleGap + AttrsUtil.bubbleHeight + AttrsUtil.bubbleArrowHeight;
            mBubble.setY(startY - bubbleY - AttrsUtil.maxRadius);

            mCircle.setX(startX - AttrsUtil.maxRadius);
            mCircle.setY(startY - AttrsUtil.maxRadius);

            mBubble.setTextThing(color, getValueString(value));
            mCircle.setRadiusColor(color);
            setBubbleCircle(VISIBLE);
        }
    }

    /**
     * 设置表格的最值以及提示值集合，该方法请在{@link #setDataLists(List, int[], boolean)} 之前调用！
     */
    public void setMaxMinLimit(float yAxisMax, float yAxisMin, float... limitValues) {
        this.yAxisMax = yAxisMax;
        this.yAxisMin = yAxisMin;
        this.limitValues = limitValues;
        postInvalidate();
    }

    /**
     * 设置渐变起止的透明度
     */
    public void setShaderAlpha(@FloatRange(from = 0.f, to = 1.f) float shaderTopAlpha, @FloatRange(from = 0.f, to = 1.f) float shaderBottomAlpha) {
        AttrsUtil.shaderTopAlpha = shaderTopAlpha;
        AttrsUtil.shaderBottomAlpha = shaderBottomAlpha;
        mChart.postInvalidate();
    }

    public void setAxisTexts(@NonNull List<String> axisTexts) {
        mXAxis.setAxisTexts(axisTexts);
    }

    /**
     * 向表格中添加数据
     *
     * @param chartDataLists 数据列表
     * @param curveColors    曲线颜色
     * @param animable       是否开启延伸动画
     */
    public void setDataLists(@NonNull List<List<ChartData>> chartDataLists,
                             @NonNull int[] curveColors, boolean animable) {
        this.clickToStart = -1f;
        this.chartDataLists = chartDataLists;
        this.curveColors = curveColors;
        postInvalidate();
        setBubbleCircle(INVISIBLE);
        mChart.setDataLists(chartDataLists, curveColors, animable);
    }

    /**
     * 设置字体的格式
     */
    public void setValueFormat(String formatStr) {
        valueFormat = new DecimalFormat(formatStr);
        postInvalidate();
    }

    /**
     * 设置上下边界线的颜色
     */
    public void setBorderColor(@ColorInt int borderColor) {
        AttrsUtil.borderColor = borderColor;
        postInvalidate();
    }

    /**
     * 设置是否可以滑动
     */
    public void setOnTouchable(boolean touchable) {
        mChart.setTouchable(touchable);
    }
}
