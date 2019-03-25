package com.lesences.lchartlib;

import android.content.res.Resources;

/**
 * @author lesences  2019/3/25 11:51.
 * @version 1.0.0
 * @description
 */
public final class Util {
    public static int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
