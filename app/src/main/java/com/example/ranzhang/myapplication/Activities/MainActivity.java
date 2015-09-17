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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.ranzhang.myapplication.Business.ChartBLL;
import com.example.ranzhang.myapplication.Filter.LowPassFilterSmoothing;
import com.example.ranzhang.myapplication.Filter.MeanFilterSmoothing;
import com.example.ranzhang.myapplication.Filter.MedianFilterSmoothing;
import com.example.ranzhang.myapplication.R;
import com.example.ranzhang.myapplication.Utils.ListUtil;
import com.example.ranzhang.myapplication.Utils.MinHeap;
import com.example.ranzhang.myapplication.Utils.Pair;
import com.example.ranzhang.myapplication.Utils.RotatingQueue;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private final int ORIGIN = 0;
    private final int LOWPASSFILTER = 1;
    private final int MEANFILTER = 2;
    private final int MEDIANFILTER = 3;

    private final int LISTNUMlIMIT = 3;
    private final int QUEUENUMlIMIT = 3;
    private int COUNT = 0;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Button button1, button2, button3;
    private ChartBLL chartbll;

    protected LowPassFilterSmoothing lpfAccelSmoothing;
    protected MedianFilterSmoothing medianFilterAccelSmoothing;
    protected MeanFilterSmoothing meanFilterAccelSmoothing;

    private LineChart mChart;

    private List<Float> pairList;
    private MinHeap<Pair<Float,Integer>> minheap;
    private RotatingQueue<Pair<Float,Integer>> queue;



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
    }

    private void initData() {
        chartbll = new ChartBLL(mChart, this);

        COUNT = 0;
        minheap = new MinHeap<Pair<Float,Integer>>(LISTNUMlIMIT);
        pairList = new ArrayList<Float>();
        queue = new RotatingQueue<Pair<Float,Integer>>(QUEUENUMlIMIT);

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
        chartbll.addEntry(calculateAcceleration(event.values), ORIGIN);

        acceleration = lpfAccelSmoothing.addSamples(event.values);
        float finalAcceleration = calculateAcceleration(acceleration);
        chartbll.addEntry(finalAcceleration, LOWPASSFILTER);

        Pair<Float, Integer> newPair = new Pair<Float, Integer>(finalAcceleration, COUNT++);
        pairList.add(finalAcceleration);
        addToMinheap(newPair);
//        acceleration = meanFilterAccelSmoothing.addSamples(event.values);
//        chartbll.addEntry(calculateAcceleration(acceleration), MEANFILTER);
//
//        acceleration = medianFilterAccelSmoothing.addSamples(event.values);
//        chartbll.addEntry(calculateAcceleration(acceleration), MEDIANFILTER);
    }

    private float calculateAcceleration(float[] acceleration) {
        if(acceleration.length != 3) {
            return 0;
        }
        double accel = Math.sqrt(Math.pow(acceleration[0], 2) + Math.pow(acceleration[1], 2) + Math.pow(acceleration[2], 2));

        return (float)accel;
    }

    private void addToMinheap(Pair<Float, Integer> newPair) {
        if (newPair == null || minheap == null) {
            return;
        }

        Pair<Float, Integer> peakPair = findPeak(newPair);
        if (peakPair != null) {
            minheap.add(peakPair);
        }
    }

    private Pair<Float, Integer> findPeak(Pair<Float, Integer> pair) {
        if (queue == null || pair == null) {
            return null;
        }

        queue.insertElement(pair);

        if (queue.existPeak()) {
            return queue.getElement(queue.size() / 2);
        } else {
            return null;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_1:
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                break;
            case R.id.btn_2:
                sensorManager.unregisterListener(this);
                analyseData(minheap);
                break;
            case R.id.btn_3:
                clearData();
                break;
            default:
                break;
        }
    }

    private void printMinHeap() {
        int size = minheap.getSize();
        for (int i = 0; i < size; i++) {
            Pair<Float, Integer> pair = minheap.minValue();
            Log.d(TAG, pair.getFirstKey() + " : " + pair.getSecondKey() + "\n");
            minheap.remove();
        }
    }

    private void analyseData(MinHeap min) {
        List<Pair<Float, Integer>> data = min.getValues();
        List<Integer> indexs = getIndexs(data);

        final Float pivot_min = Float.MAX_VALUE;
        int firstLowIndex = ListUtil.findMin(indexs.get(0), indexs.get(1), pairList, pivot_min);
        int secondLowIndex = ListUtil.findMin(indexs.get(1), indexs.get(2), pairList, pivot_min);
        int interval = secondLowIndex - firstLowIndex;

        Log.d(TAG, "firstLowIndex = " + firstLowIndex + "\n");
        Log.d(TAG, "secondLowIndex = " + secondLowIndex + "\n");
        Log.d(TAG, "Interval = " + interval + "\n");
    }

    private List<Integer> getIndexs(List<Pair<Float, Integer>> data) {
        if (data == null) {
            return null;
        }

        List<Integer> res = new ArrayList<Integer>();

        for (int i = 0; i < data.size(); i++) {
            res.add(data.get(i).getSecondKey());
            Log.d(TAG, "peak" + data.get(i).getSecondKey() + "\n");
        }
        Collections.sort(res);
        return res;
    }


    private void clearData() {
        chartbll.clearData();
        COUNT = 0;
        minheap.clear();
        pairList.clear();
        queue.clear();
        lpfAccelSmoothing.reset();
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

        return Float.valueOf(prefs.getString("lowPass", "0.2"));
    }

}