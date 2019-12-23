package kr.ac.inha.nsl.receivers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import kr.ac.inha.nsl.DatabaseHelper;
import kr.ac.inha.nsl.FromServiceRunnable;
import kr.ac.inha.nsl.R;
import kr.ac.inha.nsl.SignInActivity;
import kr.ac.inha.nsl.Tools;

import static android.content.Context.MODE_PRIVATE;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ConnectionMonitor extends ConnectivityManager.NetworkCallback {
    private static final String TAG = "ConnectionMonitor";
    private final NetworkRequest networkRequest;

    private DatabaseHelper db;
    private Context context;

    public ConnectionMonitor(Context con) {
        context = con;
        //init DB
        db = new DatabaseHelper(context);

        networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
    }

    public void enable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    public void disable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(this);
    }


    @Override
    public void onAvailable(Network network) {
        // Do what you need to do here
        Log.e(TAG, "Internet Re-connected");
        submitEMAFromDB();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //FileHelper.submitSensorData(context);
    }

    //function to submit the data when internet connection is established
    private void submitEMAFromDB() {

        //region Extracting raw data from local db and
        List<String[]> results_temp = db.getEMAData();
        int count = results_temp.size();

        if (count > 0) {
            for (String[] raw : results_temp) {
                submitEMAData(Short.parseShort(raw[0]),
                        Long.parseLong(raw[1]),
                        raw[2]);
            }
        }
    }

    private void submitEMAData(short emaOrder, long timestamp, String answers) {
        final SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", MODE_PRIVATE);
        Tools.execute(new FromServiceRunnable(
                context.getString(R.string.url_ema_submit, context.getString(R.string.server_ip)),
                loginPrefs.getString(SignInActivity.user_id, null),
                loginPrefs.getString(SignInActivity.password, null),
                emaOrder,
                timestamp,
                answers
        ) {
            @Override
            public void run() {
                String url = (String) args[0];
                String id = (String) args[1];
                String password = (String) args[2];
                short emaOrder = (short) args[3];
                long timestamp = (long) args[4];
                String answers = (String) args[5];


                try {
                    JSONObject body = new JSONObject();
                    body.put("username", id);
                    body.put("password", password);
                    body.put("ema_timestamp", timestamp);
                    body.put("ema_order", emaOrder);
                    body.put("answers", answers);

                    JSONObject json = new JSONObject(Tools.post(url, body));
                    switch (json.getInt("result")) {
                        case Tools.RES_OK:
                            Log.d(TAG, "Automatic submission of EMA");
                            deleteSubmittedRaw(timestamp);
                            break;
                        case Tools.RES_FAIL:
                            Thread.sleep(2000);
                            Log.e(TAG, "Automatic submission Failed");
                            break;
                        case Tools.RES_SRV_ERR:
                            Thread.sleep(2000);
                            Log.e(TAG, "Automatic submission Failed (SERVER)");
                            break;
                        default:
                            break;
                    }
                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to submit to server");
                }
            }
        });
    }

    //function to delete the row from local db by id
    private void deleteSubmittedRaw(long timestamp) {
        int deletedRows;
        deletedRows = db.deleteEMAData(timestamp);
        if (deletedRows > 0) {
            Log.d(TAG, "Deleted form local DB");
        } else
            Log.d(TAG, "Not deleted from local DB");
    }
}
