package kr.ac.inha.nsl.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import kr.ac.inha.nsl.DatabaseHelper;
import kr.ac.inha.nsl.Tools;
import kr.ac.inha.nsl.services.LocationService;

import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_ACTIVITY;

public class ActivityRecognitionReceiver extends BroadcastReceiver {

    public static final String TAG = "ActivityRecog";
    static boolean isDynamicActivity = false;
    static boolean isStill = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            DatabaseHelper db = new DatabaseHelper(context);
            Intent locationServiceIntent = new Intent(context, LocationService.class);
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

                DetectedActivity detectedActivity = result.getMostProbableActivity();
                String activity;

                switch (detectedActivity.getType()) {
                    case DetectedActivity.STILL:
                        activity = "STILL";
                        break;
                    case DetectedActivity.WALKING:
                        activity = "WALKING";
                        break;
                    case DetectedActivity.RUNNING:
                        activity = "RUNNING";
                        break;
                    case DetectedActivity.ON_BICYCLE:
                        activity = "ON_BICYCLE";
                        break;
                    case DetectedActivity.IN_VEHICLE:
                        activity = "IN_VEHICLE";
                        break;
                    case DetectedActivity.ON_FOOT:
                        activity = "ON_FOOT";
                        break;
                    case DetectedActivity.TILTING:
                        activity = "TILTING";
                        break;
                    case DetectedActivity.UNKNOWN:
                        activity = "UNKNOWN";
                        break;
                    default:
                        activity = "N/A";
                        break;
                }
                float confidence = ((float) detectedActivity.getConfidence()) / 100;
                db.insertSensorData(DATA_SRC_ACTIVITY, result.getTime() + " " + activity + " " + confidence);
                //Log.e("ACTIVITY UPDATE", String.format(Locale.getDefault(), "(Activity,Confidence)=(%s, %.3f)", activity, confidence));

                if (detectedActivity.getType() == DetectedActivity.STILL) {
                    isDynamicActivity = false;
                    if (confidence < 0.5)
                        isStill = false;
                } else {
                    isStill = false;
                    if (confidence < 0.5)
                        isDynamicActivity = false;
                }

                if (isDynamicActivity) { //if two consecutive dynamic activities
                    Log.e(TAG, "Two consecutive dynamic activities");
                    if (!Tools.isLocationServiceRunning(context))
                        context.startService(locationServiceIntent);
                } else if (isStill) { //if two consecutive still states
                    Log.e(TAG, "Two consecutive stills");
                    if (Tools.isLocationServiceRunning(context))
                        context.stopService(locationServiceIntent);
                }

                if (detectedActivity.getType() != DetectedActivity.STILL && confidence > 0.5) {
                    isDynamicActivity = true;
                } else if (detectedActivity.getType() == DetectedActivity.STILL && confidence > 0.5) {
                    isStill = true;
                }
            }
        }
    }
}
