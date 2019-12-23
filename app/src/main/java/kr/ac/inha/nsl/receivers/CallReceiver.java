package kr.ac.inha.nsl.receivers;

import android.content.Context;
import android.util.Log;

import kr.ac.inha.nsl.DatabaseHelper;
import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_PHONE_CALLS;

public class CallReceiver extends PhonecallReceiver {
    public static final String TAG = "CallReceiver";
    final String CALL_TYPE_OUTGOING = "OUT";
    final String CALL_TYPE_INCOMING = "IN";

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, long start, long end) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        Log.e(TAG, "onOutgoingCallEnded -> " + "number: " + number + "; start date: " + start + "; end date: " + end);
        long duration = (end - start) / 1000; // in seconds
        String value = start + " " + end + " " + CALL_TYPE_OUTGOING + " " + duration;
        db.insertSensorData(DATA_SRC_PHONE_CALLS, value);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, long start, long end) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        Log.e(TAG, "onIncomingCallEnded -> " + "number: " + number + "; start date: " + start + "; end date: " + end);
        long duration = (end - start) / 1000; // in seconds
        String value = start + " " + end + " " + CALL_TYPE_INCOMING + " " + duration;
        db.insertSensorData(DATA_SRC_PHONE_CALLS, value);
    }

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, long start) {
        Log.e(TAG, "onIncomingCallReceived -> " + "number: " + number + "; start date: " + start);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, long start) {
        Log.e(TAG, "onIncomingCallAnswered -> " + "number: " + number + "; start date: " + start);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, long start) {
        Log.e(TAG, "onOutgoingCallStarted -> " + "number: " + number + "; start date: " + start);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, long start) {
        Log.e(TAG, "onMissedCall -> " + "number: " + number + "; start date: " + start);
    }
}
