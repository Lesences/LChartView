package com.lesences.lchartview;

import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lesences.lchartlib.ChartData;
import com.lesences.lchartlib.ChartView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ChartView mChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mChartView = findViewById(R.id.cv_main);
        mChartView.post(new Runnable() {
            @Override
            public void run() {
                initData(false);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initData(true);
            }
        }, 3000);
    }


    private void initData(boolean enable) {
        int size = 12;
        List<String> xData = new ArrayList<>(size);
        List<ChartData> chartData = new ArrayList<>(size);
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            xData.add(String.format(Locale.getDefault(), "%02d", i));
            ChartData data = new ChartData();
            data.setIndex(i);

            if (i % 5 == 0 && i != 0) {
                data.setEmpty(true);
            } else {
                data.setValue(random.nextFloat() * 100);
            }

            chartData.add(data);
        }
        List<List<ChartData>> chartList = new ArrayList<>();
        chartList.add(chartData);
        mChartView.setMaxMinLimit(100f, 0f, enable ? 50f : 60f);
        mChartView.setDataLists(chartList, new int[]{Color.parseColor("#489af5")}, enable);
        mChartView.setAxisTexts(xData);
    }
}
