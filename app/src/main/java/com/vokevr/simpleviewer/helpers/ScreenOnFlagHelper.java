package com.vokevr.simpleviewer.helpers;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.vokevr.simpleviewer.helpers.SensorReadingStats;

public class ScreenOnFlagHelper implements SensorEventListener {
    private static final String TAG = "ScreenOnFlagHelper";
    private static final boolean DEBUG = false;
    private static final long IDLE_TIMEOUT_MS = 30000L;
    private static final long SAMPLE_INTERVAL_MS = 250L;
    private static final int NUM_SAMPLES = 120;
    private static final float SENSOR_THRESHOLD = 0.2F;
    private boolean screenAlwaysOn = false;
    private Activity activity;
    private SensorReadingStats sensorStats = new SensorReadingStats(120, 3);
    private long lastSampleTimestamp = 0L;
    private boolean isFlagSet = false;
    private SensorManager sensorManager;
    private Sensor sensor;

    public ScreenOnFlagHelper(Activity activity) {
        this.activity = activity;
    }

    void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void setScreenAlwaysOn(boolean enabled) {
        this.screenAlwaysOn = enabled;
        this.updateFlag();
    }

    public void start() {
        if(this.sensorManager == null) {
            this.sensorManager = (SensorManager)this.activity.getSystemService(Context.SENSOR_SERVICE);
        }

        if(this.sensor == null) {
            this.sensor = this.sensorManager.getDefaultSensor(1);
        }

        this.isFlagSet = false;
        this.setKeepScreenOnFlag(true);
        this.sensorStats.reset();
        int intervalMicros = 250000;
        this.sensorManager.registerListener(this, this.sensor, intervalMicros);
    }

    public void stop() {
        if(this.sensorManager != null) {
            this.sensorManager.unregisterListener(this);
        }

        this.setKeepScreenOnFlag(false);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        long deltaMs = (event.timestamp - this.lastSampleTimestamp) / 1000000L;
        if(deltaMs >= 250L) {
            this.sensorStats.addSample(event.values);
            this.lastSampleTimestamp = event.timestamp;
            this.updateFlag();
        }
    }

    private void updateFlag() {
        if(!this.screenAlwaysOn && this.sensorStats.statsAvailable()) {
            float maxAbsDev = this.sensorStats.getMaxAbsoluteDeviation();
            this.setKeepScreenOnFlag(maxAbsDev > 0.2F);
        } else {
            this.setKeepScreenOnFlag(true);
        }
    }

    private void setKeepScreenOnFlag(boolean on) {
        if(on != this.isFlagSet) {
            if(on) {
                this.activity.getWindow().addFlags(128);
            } else {
                this.activity.getWindow().clearFlags(128);
            }

            this.isFlagSet = on;
        }
    }
}