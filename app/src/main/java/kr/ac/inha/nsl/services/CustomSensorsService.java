package kr.ac.inha.nsl.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.util.Log;


import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import kr.ac.inha.nsl.DatabaseHelper;
import kr.ac.inha.nsl.R;
import kr.ac.inha.nsl.receivers.ActivityTransitionsReceiver;
import kr.ac.inha.nsl.receivers.SignificantMotionDetector;
import kr.ac.inha.nsl.Tools;
import kr.ac.inha.nsl.receivers.ActivityRecognitionReceiver;
import kr.ac.inha.nsl.receivers.CallReceiver;
import kr.ac.inha.nsl.receivers.PhoneUnlockedReceiver;

public class CustomSensorsService extends Service implements SensorEventListener {
    private static final String TAG = "CustomSensorsService";

    //region Constants
    private static final int ID_SERVICE = 101;
    public static final int EMA_NOTIFICATION_ID = 1234; //in sec
    public static final long EMA_NOTIF_EXPIRE = 3600;  //in sec
    public static final int EMA_BTN_VISIBLE_X_MIN_AFTER_EMA = 60; //min
    public static final int SERVICE_START_X_MIN_BEFORE_EMA = 4 * 60; //min
    public static final short HEARTBEAT_PERIOD = 5;  //in min
    public static final short APP_USAGE_SEND_PERIOD = 30;  //in sec
    public static final short DATA_SUBMIT_PERIOD = 10;  //in min
    private static final short LIGHT_SENSOR_READ_PERIOD = 5 * 60;  //in sec
    private static final short AUDIO_RECORDING_PERIOD = 5 * 60;  //in sec
    private static final int ACTIVITY_RECOGNITION_INTERVAL = 20; //in sec


    public static final short DATA_SRC_ACC = 1;
    public static final short DATA_SRC_STATIONARY_DUR = 2;
    public static final short DATA_SRC_SIGNIFICANT_MOTION = 3;
    public static final short DATA_SRC_STEP_DETECTOR = 4;
    public static final short DATA_SRC_UNLOCKED_DUR = 5;
    public static final short DATA_SRC_PHONE_CALLS = 6;
    public static final short DATA_SRC_LIGHT = 7;
    public static final short DATA_SRC_APP_USAGE = 8;
    public static final short DATA_SRC_GPS_LOCATIONS = 9;
    public static final short DATA_SRC_ACTIVITY = 10;
    public static final short DATA_SRC_TOTAL_DIST_COVERED = 11;
    public static final short DATA_SRC_MAX_DIST_FROM_HOME = 12;
    public static final short DATA_SRC_MAX_DIST_TWO_LOCATIONS = 13;
    public static final short DATA_SRC_RADIUS_OF_GYRATION = 14;
    public static final short DATA_SRC_STDDEV_OF_DISPLACEMENT = 15;
    public static final short DATA_SRC_NUM_OF_DIF_PLACES = 16;
    public static final short DATA_SRC_AUDIO_LOUDNESS = 17;
    public static final short DATA_SRC_ACTIVITY_DURATION = 18;
    //endregion

    DatabaseHelper db;

    //private StationaryDetector mStationaryDetector;
    NotificationManager mNotificationManager;
    private SensorManager mSensorManager;
    private Sensor sensorStepDetect;
    private Sensor sensorSM;
    private Sensor sensorAcc;

    private SignificantMotionDetector mSMListener;

    private PhoneUnlockedReceiver mPhoneUnlockedReceiver;
    private CallReceiver mCallReceiver;


    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent activityRecPendingIntent;

    Intent stationaryDetector;

    private ActivityRecognitionClient activityTransitionClient;
    private PendingIntent activityTransPendingIntent;

    ScheduledExecutorService dataSubmitScheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        db = new DatabaseHelper(this);

        activityRecognitionClient = ActivityRecognition.getClient(getApplicationContext());
        activityRecPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, new Intent(getApplicationContext(), ActivityRecognitionReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        activityRecognitionClient.requestActivityUpdates(ACTIVITY_RECOGNITION_INTERVAL * 1000, activityRecPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Registered: Activity Recognition");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed: Activity Recognition");
                    }
                });

        activityTransitionClient = ActivityRecognition.getClient(getApplicationContext());
        activityTransPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), ActivityTransitionsReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        activityTransitionClient.requestActivityTransitionUpdates(new ActivityTransitionRequest(getActivityTransitions()), activityTransPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Registered: Activity Transition");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed: Activity Transition " + e.toString());
                    }
                });

        stationaryDetector = new Intent(this, StationaryDetector.class);
        startService(stationaryDetector);

        //region Register ACC sensor
        sensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensorAcc != null) {
            mSensorManager.registerListener(CustomSensorsService.this, sensorAcc, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.e(TAG, "Acc sensor is NOT available");
        }
        //endregion

        //region Register Step detector sensor
        sensorStepDetect = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (sensorStepDetect != null) {
            mSensorManager.registerListener(this, sensorStepDetect, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.e(TAG, "Step detector sensor is NOT available");
        }
        //endregion

        //region Register Significant motion sensor
        mSMListener = new SignificantMotionDetector(this, db);
        sensorSM = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        if (sensorSM != null) {
            mSensorManager.requestTriggerSensor(mSMListener, sensorSM);
        } else {
            Log.e(TAG, "Significant motion sensor is NOT available");
        }
        //endregion

        //region Register Phone unlock state receiver
        mPhoneUnlockedReceiver = new PhoneUnlockedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mPhoneUnlockedReceiver, filter);
        //endregion

        //region Register Phone call logs receiver
        mCallReceiver = new CallReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.addAction(Intent.EXTRA_PHONE_NUMBER);
        registerReceiver(mCallReceiver, intentFilter);
        //endregion

        //region Posting Foreground notification when service is started
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channel_id = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel() : "";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.mipmap.ic_launcher_no_bg)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);
        Notification notification = builder.build();
        startForeground(ID_SERVICE, notification);
        //endregion
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public String createNotificationChannel() {
        String id = "YouNoOne_channel_id";
        String name = "You no one channel id";
        String description = "This is description";
        NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(mChannel);
        return id;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //region Unregister listeners
        mSensorManager.cancelTriggerSensor(mSMListener, sensorSM);
        mSensorManager.unregisterListener(this, sensorAcc);
        mSensorManager.unregisterListener(this, sensorStepDetect);
        activityRecognitionClient.removeActivityUpdates(activityRecPendingIntent);
        activityTransitionClient.removeActivityTransitionUpdates(activityTransPendingIntent);
        unregisterReceiver(mPhoneUnlockedReceiver);
        unregisterReceiver(mCallReceiver);

        stationaryDetector = new Intent(this, StationaryDetector.class);
        stopService(stationaryDetector);
        //endregion

        //region Stop foreground service
        stopForeground(false);
        mNotificationManager.cancel(ID_SERVICE);
        //endregion

        Tools.sleep(1000);

        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String value = System.currentTimeMillis() + " " + event.values[0] + " " + event.values[1] + " " + event.values[2] + " " + Tools.getEMAOrderFromRangeBeforeEMA(System.currentTimeMillis());
            db.insertSensorData(DATA_SRC_ACC, value);
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            db.insertSensorData(DATA_SRC_STEP_DETECTOR, System.currentTimeMillis() + "");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public List<ActivityTransition> getActivityTransitions() {
        List<ActivityTransition> transitionList = new ArrayList<>();
        ArrayList<Integer> activities = new ArrayList<>(Arrays.asList(
                DetectedActivity.STILL,
                DetectedActivity.WALKING,
                DetectedActivity.RUNNING,
                DetectedActivity.ON_BICYCLE,
                DetectedActivity.IN_VEHICLE));
        for (int activity : activities) {
            transitionList.add(new ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER).build());

            transitionList.add(new ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT).build());
        }

        return transitionList;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
