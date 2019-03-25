package com.lesences.lchartlib;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * @author lesences  2018/1/22 11:07.
 */
class BaseChart extends View {
    private final static String TAG = BaseChart.class.getSimpleName();
    private final static int DEFAULT_LINE_NUM = 2;
    protected OverScroller mOverScroller;
    protected VelocityTracker mVelocityTracker;
    //惯性最大最小速度
    protected int mMaximumVelocity;
    protected int mMinimumVelocity;
    //最大移动长度
    private int mMaxScrollX;
    //单位长度
    float mUnit;
    //上次点击的
    private float mLastEventX = 0.f;
    private float mLastIndex = -1.f;
    private ChartView mParent;
    private List<List<ChartData>> chartDataLists = new ArrayList<>(DEFAULT_LINE_NUM);
    private SparseArray<List<RectF>> rectfAreaSa = new SparseArray<>(DEFAULT_LINE_NUM);
    private SparseArray<Path> curvePathSa = new SparseArray<>(DEFAULT_LINE_NUM);
    private SparseArray<Path> shaderPathSa = new SparseArray<>(DEFAULT_LINE_NUM);
    private SparseArray<Float> maxValueSa = new SparseArray<>(DEFAULT_LINE_NUM);
    private int lastScrollX = -1;
    private boolean isAnimating = false;
    private int[] curveColors = null;
    private int[] shaderColors = new int[2];

    private GestureDetector mGestureDetector;
    private MotionEvent clickEvent = null;

    //path动画百分比
    private float pathPercent = 1.f;
    private Path animatorPath = new Path();
    private ValueAnimator pathAnimator = null;
    private PathMeasure mPathMeasure = new PathMeasure();

    private boolean touchable = true;

    private BaseChart(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                clickEvent = e;
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                //按下的回调
                clickEvent = null;
                mParent.scrollStatu(false, -1f);
                return super.onDown(e);
            }
        };
        mGestureDetector = new GestureDetector(context, simpleOnGestureListener);
    }

    BaseChart(Context context, ChartView mParent) {
        this(context);
        this.mParent = mParent;
        init(context);
    }

    private void init(Context context) {
        mOverScroller = new OverScroller(context);
        //配置速度
        initVelocityTracker();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mUnit = (w - (AttrsUtil.curveWidth + AttrsUtil.minRadius) * 2) / (float) AttrsUtil.visibleCount;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != pathAnimator && pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        recycleVelocityTracker();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int size = chartDataLists.size();
        if (null == curveColors || size != curveColors.length) {
            return;
        }

        int curScorllX = getScrollX();
        if (lastScrollX != curScorllX) {
            clearDrawSparses();
            int startIndex = Math.round(curScorllX / mUnit);
            int endIndex = startIndex + AttrsUtil.visibleCount;
            for (int i = 0; i < size; i++) {
                Float maxValue = maxValueSa.get(i, null);
                if (null == maxValue) {
                    continue;
                }
                initValidList(startIndex, endIndex, i);
            }
            lastScrollX = curScorllX;
        }

        if (size != shaderPathSa.size() || size != maxValueSa.size() || size != curvePathSa.size()) {
            return;
        }

        for (int i = 0; i < size; i++) {
            Path curvePath = curvePathSa.get(i, null);
            int curveColor = curveColors[i];
            drawPaths(canvas, curvePath, curveColor, i);
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnimating || !touchable) {
            return false;
        }
        mGestureDetector.onTouchEvent(event);
        initVelocityTracker();
        mVelocityTracker.addMovement(event);
        float eventX = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mOverScroller.isFinished()) {
                    mOverScroller.abortAnimation();
                }
                mLastEventX = eventX;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = mLastEventX - eventX;
                if (Math.abs(moveX) < 5) {
                    break;
                }
                mLastEventX = eventX;
                scrollBy((int) (moveX), 0);
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) mVelocityTracker.getXVelocity();
                if (Math.abs(velocityX) > mMinimumVelocity) {
                    fling(-velocityX);
                } else {
                    scrollToNearest();
                }
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mOverScroller.isFinished()) {
                    mOverScroller.abortAnimation();
                }
                scrollToNearest();
                recycleVelocityTracker();
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            scrollTo(mOverScroller.getCurrX(), mOverScroller.getCurrY());
            //这是最后OverScroller的最后一次滑动
            if (!mOverScroller.computeScrollOffset()) {
                scrollToNearest();
            }
            postInvalidate();
        }
    }


    @Override
    public void scrollTo(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (x > mMaxScrollX) {
            x = mMaxScrollX;
        }
        if (x != getScrollX()) {
            mParent.onChartScrollX(x, mUnit);
            super.scrollTo(x, y);
            mLastIndex = scrollXtoEndIndex(x);
        }
    }

    void setDataLists(@NonNull List<List<ChartData>> chartDataLists, @NonNull int[] curveColors, boolean animable) {
        int size = chartDataLists.size();
        if (size != curveColors.length) {
            return;
        }
        if (null != pathAnimator && pathAnimator.isRunning()) {
            pathAnimator.cancel();
        }
        if (!mOverScroller.isFinished()) {
            mOverScroller.abortAnimation();
        }
        this.chartDataLists.clear();
        this.chartDataLists.addAll(chartDataLists);
        this.curveColors = curveColors;

        this.lastScrollX = -1;
        this.maxValueSa.clear();

        int maxItemSize = 0;
        for (int i = 0; i < size; i++) {
            List<ChartData> chartDatas = chartDataLists.get(i);
            int itemSize = chartDatas.size();
            maxItemSize = Math.max(maxItemSize, itemSize);
            Float maxValue = null;
            for (ChartData itemChart : chartDatas) {
                if (itemChart.isEmpty()) {
                    continue;
                }
                float itemValue = itemChart.getValue();
                if (null == maxValue) {
                    maxValue = itemValue;
                } else {
                    maxValue = Math.max(maxValue, itemValue);
                }
            }
            maxValueSa.put(i, maxValue);
        }
        mMaxScrollX = indexToScrollX(maxItemSize);

        goToIndex(maxItemSize);
        if (animable) {
            startPathAnimator();
        }
    }

    /**
     * 设置是否可以滑动
     */
    void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    /**
     * 清空缓存的数据
     */
    private void clearDrawSparses() {
        this.curvePathSa.clear();
        this.shaderPathSa.clear();
        this.rectfAreaSa.clear();
    }

    /**
     * 截取起始点的上一个有效点，末尾点的下一个有效点，
     */
    private void initValidList(int startIndex, int endIndex, int index) {
        List<ChartData> chartDatas = chartDataLists.get(index);
        if (startIndex <= 0) {
            startIndex = 0;
        } else {
            ChartData last = getLastIsNotEmpty(startIndex, chartDatas);
            if (null != last) {
                startIndex = last.getIndex();
            }
        }
        int listSize = chartDatas.size();
        if (endIndex >= listSize - 1) {
            endIndex = listSize - 1;
        } else {
            ChartData next = getNextIsNotEmpty(endIndex, listSize, chartDatas);
            if (null != next) {
                endIndex = next.getIndex();
            }
        }
        List<ChartData> validList = chartDatas.subList(startIndex, endIndex + 1);
        initCurvePaths(index, validList);
    }

    /**
     * 获取上一个有效的点
     */
    private ChartData getLastIsNotEmpty(int index, List<ChartData> chartDatas) {
        for (int i = index - 1; i >= 0; i--) {
            ChartData item = chartDatas.get(i);
            if (item.isEmpty()) {
                continue;
            }
            return item;
        }
        return null;
    }

    /**
     * 获取下一个有效的点
     */
    private ChartData getNextIsNotEmpty(int index, int listSize, List<ChartData> chartDatas) {
        for (int i = index + 1; i < listSize; i++) {
            ChartData item = chartDatas.get(i);
            if (item.isEmpty()) {
                continue;
            }
            return item;
        }
        return null;
    }

    /**
     * 回弹到最近的一个点
     */
    private void scrollToNearest() {
        int curScorllX = getScrollX();
        BigDecimal bigDecimal = new BigDecimal(mLastIndex);
        bigDecimal = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP);
        if (bigDecimal.floatValue() == Math.round(mLastIndex)) {
            if (null != clickEvent) {
                clickWhere(clickEvent.getX(), clickEvent.getY(), curScorllX);
                clickEvent = null;
            }
            //滑动结束的回调
            mParent.scrollStatu(true, curScorllX);
            return;
        }
        mOverScroller.startScroll(curScorllX, 0, indexToScrollX(Math.round(mLastIndex)) - curScorllX, 0, 250);
        postInvalidate();
    }

    /**
     * 初始化当前窗口的绘图数据
     */
    private void initCurvePaths(int keySa, List<ChartData> chartDatas) {
        if (chartDatas.size() < 1) {
            shaderPathSa.put(keySa, null);
            curvePathSa.put(keySa, null);
            rectfAreaSa.put(keySa, null);
            return;
        }
        List<RectF> areaRectF = new ArrayList<>();
        for (ChartData item : chartDatas) {
            if (item.isEmpty()) {
                continue;
            }
            float toLeft = getIndexToStart(item.getIndex());
            float toTop = getValueToTop(item.getValue());
            areaRectF.add(getAreaRectf(toLeft, toTop));
        }
        if (areaRectF.isEmpty()) {
            shaderPathSa.put(keySa, null);
            curvePathSa.put(keySa, null);
            rectfAreaSa.put(keySa, null);
            return;
        }
        rectfAreaSa.put(keySa, areaRectF);
        int rectfSize = areaRectF.size();

        Path curvePath = new Path();
        RectF pre = areaRectF.get(0);
        RectF cur = pre;
        float firstX = cur.centerX();
        curvePath.moveTo(firstX, cur.centerY());
        for (int i = 1; i < rectfSize; i++) {
            pre = cur;
            cur = areaRectF.get(i);
            float preX = pre.centerX();
            float curX = cur.centerX();
            float cpX = (curX + preX) * 0.5f;
            float preY = pre.centerY();
            float curY = cur.centerY();

            curvePath.cubicTo(cpX, preY, cpX, curY, curX, curY);
        }
        curvePathSa.put(keySa, curvePath);

        Path shaderPath = new Path(curvePath);
        RectF last = areaRectF.get(rectfSize - 1);
        float bottomY = getChartBottom();
        shaderPath.lineTo(last.centerX(), bottomY);
        shaderPath.lineTo(firstX, bottomY);
        shaderPath.close();
        shaderPathSa.put(keySa, shaderPath);
    }

    /**
     * 绘制曲线/圆点等
     */
    private void drawPaths(Canvas canvas, Path curvePath, int curveColor, int index) {
        if (!isAnimating) {
            Float maxValue = maxValueSa.get(index, null);
            if (null != maxValue) {//绘制渐变效果
                Path shaderPath = shaderPathSa.get(index, null);
                int red = Color.red(curveColor);
                int green = Color.green(curveColor);
                int blue = Color.blue(curveColor);
                int topAlpha = (int) (255 * AttrsUtil.shaderTopAlpha);
                int bottomAlpha = (int) (255 * AttrsUtil.shaderBottomAlpha);
                shaderColors[0] = Color.argb(topAlpha, red, green, blue);
                shaderColors[1] = Color.argb(bottomAlpha, red, green, blue);
                Shader shader = getGradient(maxValue);
                mParent.curvePaint.setShader(shader);
                mParent.curvePaint.setStyle(Paint.Style.FILL);
                canvas.drawPath(shaderPath, mParent.curvePaint);
            }
            //绘制曲线
            if (null != curvePath) {
                mParent.curvePaint.setColor(curveColor);
                mParent.curvePaint.setShader(null);
                mParent.curvePaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(curvePath, mParent.curvePaint);
            }

            //绘制曲线上的点
            List<RectF> points = rectfAreaSa.get(index, null);
            if (null == points || points.isEmpty()) {
                return;
            }
            for (RectF point : points) {
                float centerX = point.centerX();
                float centerY = point.centerY();
                mParent.curvePaint.setStyle(Paint.Style.FILL);
                mParent.curvePaint.setColor(Color.WHITE);
                canvas.drawCircle(centerX, centerY, AttrsUtil.minRadius, mParent.curvePaint);

                mParent.curvePaint.setColor(curveColor);
                mParent.curvePaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(centerX, centerY, AttrsUtil.minRadius, mParent.curvePaint);
            }
        } else {
            //绘制延伸动画曲线
            if (null != curvePath) {
                mParent.curvePaint.setColor(curveColor);
                mParent.curvePaint.setShader(null);
                mParent.curvePaint.setStyle(Paint.Style.STROKE);

                mPathMeasure.setPath(curvePath, false);
                animatorPath.reset();
                mPathMeasure.getSegment(0, pathPercent * mPathMeasure.getLength(), animatorPath, true);
                canvas.drawPath(animatorPath, mParent.curvePaint);
            }
        }
    }

    /**
     * 跳转到相应位置
     */
    private void goToIndex(int index) {
        scrollTo(indexToScrollX(index), 0);
    }

    /**
     * 把滑动偏移量scrollX转化为endIndex
     */
    private float scrollXtoEndIndex(float scrollX) {
        return scrollX / mUnit + AttrsUtil.visibleCount + 1;
    }

    /**
     * 把index转化为scrollX
     */
    private int indexToScrollX(float index) {
        float tempIndex = index - (AttrsUtil.visibleCount + 1);
        if (tempIndex <= 0) {
            return 0;
        }
        return Math.round(tempIndex * mUnit);
    }

    private void fling(int vX) {
        mOverScroller.fling(getScrollX(), 0, vX, 0, 0, mMaxScrollX, 0, 0);
        postInvalidate();
    }

    private void initVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private RectF getAreaRectf(float pointX, float pointY) {
        return new RectF(pointX - AttrsUtil.clickRadius, pointY - AttrsUtil.clickRadius,
                pointX + AttrsUtil.clickRadius, pointY + AttrsUtil.clickRadius);
    }

    private float getIndexToStart(float index) {
        return AttrsUtil.minRadius + AttrsUtil.curveWidth + index * mUnit;
    }

    private float getValueToTop(float value) {
        return mParent.getValueToTop(value) - mParent.topMargin + AttrsUtil.minRadius + AttrsUtil.lineWidth;
    }

    private float getChartBottom() {
        return mParent.getChartBottom();
    }

    private LinearGradient getGradient(float maxValue) {
        float startX = 0;
        return new LinearGradient(startX, getValueToTop(maxValue),
                startX, getChartBottom(), shaderColors,
                null, Shader.TileMode.CLAMP);
    }

    private void clickWhere(float x, final float y, float scrollX) {
        x += getScrollX();
        int size = rectfAreaSa.size();
        ClickData nearestClick = null;
        for (int i = 0; i < size; i++) {
            List<RectF> itemRectFs = rectfAreaSa.get(i, null);
            if (null == itemRectFs) {
                continue;
            }
            for (RectF itemRectf : itemRectFs) {
                if (itemRectf.contains(x, y)) {
                    if (null == nearestClick) {
                        nearestClick = new ClickData(itemRectf, i);
                    } else {
                        nearestClick = getNearestClick(nearestClick, itemRectf, x, y, i);
                    }
                }
            }
        }
        mParent.clickEvent(nearestClick);
    }

    private ClickData getNearestClick(ClickData nearestClick, RectF curRectf, float x, final float y, int i) {
        RectF lastRectf = nearestClick.getRectF();
        float lastLength2 = getLineLength2(lastRectf, x, y);
        float curLength2 = getLineLength2(curRectf, x, y);
        if (lastLength2 <= curLength2) {
            return nearestClick;
        } else {
            nearestClick.setCurveNO(i);
            nearestClick.setRectF(curRectf);
            return nearestClick;
        }
    }

    private float getLineLength2(RectF rectF, float x, float y) {
        float tempX = x - rectF.centerX();
        float tempY = y - rectF.centerY();
        return tempX * tempX + tempY * tempY;
    }

    private void startPathAnimator() {
        pathAnimator = ValueAnimator.ofFloat(0.f, 1.f).setDuration(AttrsUtil.animatorDuration);
        pathAnimator.setInterpolator(AttrsUtil.interpolator);
        pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                pathPercent = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        pathAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                postInvalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
                postInvalidate();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        pathAnimator.start();
    }

}
