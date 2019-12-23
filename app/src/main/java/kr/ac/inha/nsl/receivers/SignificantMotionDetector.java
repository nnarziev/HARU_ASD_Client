package kr.ac.inha.nsl.receivers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;

import kr.ac.inha.nsl.DatabaseHelper;

import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_SIGNIFICANT_MOTION;


public class SignificantMotionDetector extends TriggerEventListener implements SensorEventListener {

    public static final String TAG = "SigMotionListener";
    private Context context;
    private DatabaseHelper db;
    private static boolean isAccSensing = false;

    public SignificantMotionDetector(Context con, DatabaseHelper db) {
        this.context = con;
        this.db = db;
    }

    @Override
    public void onTrigger(TriggerEvent event) {
        long current_sensor_time = System.currentTimeMillis();
        if (event.values[0] == 1) {
            final SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            final Sensor sensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            final Sensor sensorSignificantMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

            //start scheduled task for accelerometer after sig motion sensor triggered
            /*Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (isAccSensing)
                        if (sensorAcc != null) {
                            mSensorManager.unregisterListener(SignificantMotionDetector.this, sensorAcc);
                            isAccSensing = false;
                        }
                }
            };
            timer.schedule(task, 30 * 1000);

            if (!isAccSensing)
                if (sensorAcc != null) {
                    mSensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_GAME);
                    isAccSensing = true;
                }*/
            db.insertSensorData(DATA_SRC_SIGNIFICANT_MOTION, current_sensor_time + "");
            mSensorManager.requestTriggerSensor(this, sensorSignificantMotion);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String value = System.currentTimeMillis() + " " + event.values[0] + " " + event.values[1] + " " + event.values[2];
            //db.insertSensorData(DATA_SRC_ACC, value);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
