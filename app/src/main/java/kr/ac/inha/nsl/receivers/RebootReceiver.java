package kr.ac.inha.nsl.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.ac.inha.nsl.SignInActivity;

public class RebootReceiver extends BroadcastReceiver {
    public static final String TAG = "RebootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent intentService = new Intent(context, SignInActivity.class);
            intentService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentService.putExtra("fromReboot", true);
            context.startActivity(intentService);
        }
        else {
            Log.e(TAG, "Received unexpected intent " + intent.toString());
        }
    }
}
