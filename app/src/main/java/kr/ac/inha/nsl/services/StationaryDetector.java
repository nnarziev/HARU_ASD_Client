package kr.ac.inha.nsl.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import kr.ac.inha.nsl.DatabaseHelper;

import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_STATIONARY_DUR;

public class StationaryDetector extends Service implements SensorEventListener {

    protected final String TAG = getClass().getSimpleName();

    private long stationaryDetectStartTime;
    private long stationaryDetectEndTime;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private DatabaseHelper db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DatabaseHelper(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stationaryDetectStartTime = System.currentTimeMillis();
        stationaryDetectEndTime = System.currentTimeMillis();
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTimestamp = System.currentTimeMillis();
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float diff = (float) Math.sqrt(x * x + y * y + z * z);

            if (diff > 0.5) // 0.5 is a threshold, you can test it and change it
            {
                if (curTimestamp - stationaryDetectEndTime > 5000) {
                    stationaryDetectEndTime = curTimestamp;
                    long duration = (stationaryDetectEndTime - stationaryDetectStartTime - 5000) / 1000; // in seconds
                    if (duration > 0) {
                        String value = (stationaryDetectStartTime - 5000) + " " + stationaryDetectEndTime + " " + duration;
                        db.insertSensorData(DATA_SRC_STATIONARY_DUR, value);
                        stationaryDetectStartTime = curTimestamp;
                    }
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, accelerometer);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
