package kr.ac.inha.nsl.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import kr.ac.inha.nsl.FromServiceRunnable;
import kr.ac.inha.nsl.R;
import kr.ac.inha.nsl.SignInActivity;
import kr.ac.inha.nsl.Tools;

import static android.content.Context.MODE_PRIVATE;

public class GeofenceReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences locationPrefs = context.getSharedPreferences("UserLocations", MODE_PRIVATE);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String error = String.valueOf(geofencingEvent.getErrorCode());
            Log.e(TAG, "Error code: " + error);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Calendar curTime = Calendar.getInstance();
            for (Geofence geofence : triggeringGeofences) {
                SharedPreferences.Editor editor = locationPrefs.edit();
                editor.putLong(geofence.getRequestId() + "_ENTERED_TIME", curTime.getTimeInMillis());
                editor.apply();
            }
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Calendar curTime = Calendar.getInstance();
            JSONArray jsonArray = new JSONArray();
            for (Geofence geofence : triggeringGeofences) {
                JSONObject location = new JSONObject();
                try {
                    location.put("id", geofence.getRequestId());
                    location.put("timestamp_enter", locationPrefs.getLong(geofence.getRequestId() + "_ENTERED_TIME", 0));
                    location.put("timestamp_exit", curTime.getTimeInMillis());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(location);

                SharedPreferences.Editor editor = locationPrefs.edit();
                editor.remove(geofence.getRequestId() + "_ENTERED_TIME");
                editor.apply();
            }
            submitLocationData(context, jsonArray);
        } else {
            // Log the error.
            Log.e(TAG, "geofence transition type error: " + geofenceTransition);
        }
    }

    private void submitLocationData(final Context con, JSONArray locationsObjArray) {
        final SharedPreferences loginPrefs = con.getSharedPreferences("UserLogin", MODE_PRIVATE);
        if (Tools.isNetworkAvailable(con))
            Tools.execute(new FromServiceRunnable(
                    con.getString(R.string.url_location_submit, con.getString(R.string.server_ip)),
                    loginPrefs.getString(SignInActivity.user_id, null),
                    loginPrefs.getString(SignInActivity.password, null),
                    locationsObjArray
            ) {
                @Override
                public void run() {
                    String url = (String) args[0];
                    String id = (String) args[1];
                    String password = (String) args[2];
                    JSONArray locationsJsonArray = (JSONArray) args[3];

                    try {
                        JSONObject body = new JSONObject();
                        body.put("username", id);
                        body.put("password", password);
                        body.put("locations", locationsJsonArray);
                        Log.e(TAG, "LOCATION JSON" + body.toString());

                        JSONObject json = new JSONObject(Tools.post(url, body));

                        switch (json.getInt("result")) {
                            case Tools.RES_OK:
                                Log.d("submitLocationData", "Locations data are submitted to server");
                                break;
                            case Tools.RES_FAIL:
                                Thread.sleep(2000);
                                Log.e("submitLocationData", "Failed to submit locations data");
                                break;
                            case Tools.RES_SRV_ERR:
                                Thread.sleep(2000);
                                Log.d("submitLocationData", "Failed to submit locations data(SERVER)");
                                break;
                            default:
                                break;
                        }
                    } catch (IOException | JSONException | InterruptedException e) {
                        e.printStackTrace();
                        Log.e("submitLocationData", "Failed to submit locations data SERVER is Down");
                    }
                }
            });
    }

    private void sendNotification(Context con, String content, boolean isEntered) {
        final NotificationManager notificationManager = (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificaiton_id = 1234;  //notif id for exit
        if (isEntered) {
            notificaiton_id = 4567;  //notif id for enter
        }

        String channelId = "geofence_notifs";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(con.getApplicationContext(), channelId);
        builder.setContentTitle(con.getString(R.string.app_name))
                .setContentText(content)
                .setTicker("New Message Alert!")
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_no_bg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, con.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        final Notification notification = builder.build();
        notificationManager.notify(notificaiton_id, notification);
    }
}
