package com.example.ranzhang.myapplication.Activities;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.ranzhang.myapplication.Business.ChartBLL;
import com.example.ranzhang.myapplication.Business.MinHeap;
import com.example.ranzhang.myapplication.Filter.LowPassFilterSmoothing;
import com.example.ranzhang.myapplication.Filter.MeanFilterSmoothing;
import com.example.ranzhang.myapplication.Filter.MedianFilterSmoothing;
import com.example.ranzhang.myapplication.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

public class MainActivity extends Activity implements SensorEventListener, OnChartValueSelectedListener, View.OnClickListener {

    private final int ORIGIN = 0;
    private final int LOWPASSFILTER = 1;
    private final int MEANFILTER = 2;
    private final int MEDIANFILTER = 3;

    private double finalAcceleration = 0;

    private final int num_Limit = 3;
    private final int setIndex = 0;
    private final int limit = 2;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Button button1, button2, button3;

    protected LowPassFilterSmoothing lpfAccelSmoothing;
    protected MedianFilterSmoothing medianFilterAccelSmoothing;
    protected MeanFilterSmoothing meanFilterAccelSmoothing;

    private LineChart mChart;
    private ChartBLL chartbll;

    private MinHeap<Double> minheap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        } else {
            finish();
        }

        init();
    }

    private void init() {
        initView();
        initData();
        initFilters();
    }

    private void initView() {
        button1 = (Button)findViewById(R.id.btn_1);
        button2 = (Button)findViewById(R.id.btn_2);
        button3 = (Button)findViewById(R.id.btn_3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setOnChartValueSelectedListener(this);
    }

    private void initData() {

        chartbll = new ChartBLL(mChart, this);
        minheap = new MinHeap<Double>();

        meanFilterAccelSmoothing = new MeanFilterSmoothing();
        medianFilterAccelSmoothing = new MedianFilterSmoothing();
        lpfAccelSmoothing = new LowPassFilterSmoothing();
    }

    private void initFilters() {
        meanFilterAccelSmoothing.setTimeConstant(getPrefMeanFilterSmoothingTimeConstant());
        medianFilterAccelSmoothing.setTimeConstant(getPrefMedianFilterSmoothingTimeConstant());
        lpfAccelSmoothing.setTimeConstant(getPrefLpfSmoothingTimeConstant());
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] acceleration;
        chartbll.addEntry(calculateAccelrate(event.values), ORIGIN);

        acceleration = lpfAccelSmoothing.addSamples(event.values);
        chartbll.addEntry(calculateAccelrate(acceleration), LOWPASSFILTER);

        acceleration = meanFilterAccelSmoothing.addSamples(event.values);
        chartbll.addEntry(calculateAccelrate(acceleration), MEANFILTER);

        acceleration = medianFilterAccelSmoothing.addSamples(event.values);
        chartbll.addEntry(calculateAccelrate(acceleration), MEDIANFILTER);
    }

    private float calculateAccelrate(float[] acceleration) {
        if(acceleration.length != 3) {
            return 0;
        }
        double accel = Math.sqrt(Math.pow(acceleration[0], 2) + Math.pow(acceleration[1], 2) + Math.pow(acceleration[2], 2));

        return (float)accel;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
//        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }

    private void filter(Double a) {
        if (minheap == null) {
            return;
        }

        if (minheap.getSize() >= num_Limit) {
            if (a < minheap.minValue()) {
                return;
            } else {
                minheap.remove();
            }
        }

        minheap.add(a);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_1:
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                break;
            case R.id.btn_2:
                sensorManager.unregisterListener(this);
                break;
            case R.id.btn_3:
                chartbll.clearData();
                break;
            default:
                break;
        }
    }


    private float getPrefMeanFilterSmoothingTimeConstant()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        return Float.valueOf(prefs.getString("mean", "0.5"));
    }

    private float getPrefMedianFilterSmoothingTimeConstant()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        return Float.valueOf(prefs.getString("median", "0.5"));
    }

    private float getPrefLpfSmoothingTimeConstant()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        return Float.valueOf(prefs.getString("lowPass", "0.5"));
    }

}