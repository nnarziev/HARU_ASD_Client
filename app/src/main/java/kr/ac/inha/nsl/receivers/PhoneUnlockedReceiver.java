package kr.ac.inha.nsl.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.inha.nsl.DatabaseHelper;

import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_UNLOCKED_DUR;

public class PhoneUnlockedReceiver extends BroadcastReceiver {
    public static final String TAG = "PhoneUnlockedReceiver";

    private long phoneUnlockedDurationStart;
    private boolean unlocked = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper db = new DatabaseHelper(context);
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.e(TAG, "Phone unlocked");
            unlocked = true;
            phoneUnlockedDurationStart = System.currentTimeMillis();

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.e(TAG, "Phone locked");
            if (unlocked) {
                unlocked = false;
                long phoneUnlockedDurationEnd = System.currentTimeMillis();
                long duration = (phoneUnlockedDurationEnd - phoneUnlockedDurationStart) / 1000; // in seconds
                String value = phoneUnlockedDurationStart + " " + phoneUnlockedDurationEnd + " " + duration;
                db.insertSensorData(DATA_SRC_UNLOCKED_DUR, value);
            }
        }
    }
}
