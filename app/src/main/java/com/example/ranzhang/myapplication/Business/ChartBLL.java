package com.example.ranzhang.myapplication.Business;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

/**
 * Created by ran.zhang on 9/16/15.
 */
public class ChartBLL {

    private LineChart mChart;
    private Context mContext;

    public ChartBLL(LineChart chart, Context context) {
        if(chart == null || context == null) {
            return;
        }

        this.mChart = chart;
        this.mContext = context;

        initChart();
        initChartData();
    }

    public void clearData() {
        LineData data = mChart.getData();
        data.clearValues();
        initChart();
        initChartData();
        mChart.invalidate();
    }

    private void initChart() {
        mChart.setDrawGridBackground(true);
        mChart.getAxisLeft().setDrawGridLines(true);
        mChart.getAxisRight().setEnabled(false);
        mChart.getXAxis().setDrawGridLines(true);
        mChart.getXAxis().setDrawAxisLine(true);
        mChart.setDescription("");
        mChart.invalidate();
    }

    private void initChartData() {

        ArrayList<String> xVals = new ArrayList<String>();

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

        addDataSet(ColorTemplate.getHoloBlue(), "Origin", dataSets);
        addDataSet(ColorTemplate.JOYFUL_COLORS[0], "LowPass", dataSets);
//        addDataSet(ColorTemplate.JOYFUL_COLORS[1], "Mean", dataSets);
//        addDataSet(ColorTemplate.JOYFUL_COLORS[3], "Median", dataSets);

        LineData data = new LineData(xVals, dataSets);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        mChart.setData(data);
    }

    private void addDataSet(int color, String description, ArrayList<LineDataSet> dataSets) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        LineDataSet set = new LineDataSet(yVals, description);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);

        dataSets.add(set);
    }

    public void addEntry(float a, int index) {

        LineData data = mChart.getData();
        LineDataSet set = data.getDataSetByIndex(index);

        int count = data.getXValCount();

        if(count == set.getEntryCount()) {
            data.addXValue(set.getEntryCount() + "");
        }

        data.addEntry(new Entry(a, set.getEntryCount()), index);

        mChart.notifyDataSetChanged();
        mChart.moveViewTo(data.getXValCount() - 10, a, YAxis.AxisDependency.LEFT);
    }



}
