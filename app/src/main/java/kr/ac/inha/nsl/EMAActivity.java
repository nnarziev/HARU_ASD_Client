package kr.ac.inha.nsl;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import static kr.ac.inha.nsl.services.CustomSensorsService.EMA_NOTIFICATION_ID;

public class EMAActivity extends AppCompatActivity {

    //region Constants
    public static final String TAG = "EMAActivity";
    public static final Short[] EMA_NOTIF_HOURS = {10, 14, 18, 22};  //in hours of day
    public static final long[] EMA_NOTIF_MILLIS = new long[]{EMA_NOTIF_HOURS[0] * 3600 * 1000, EMA_NOTIF_HOURS[1] * 3600 * 1000, EMA_NOTIF_HOURS[2] * 3600 * 1000, EMA_NOTIF_HOURS[3] * 3600 * 1000};  //in milliseconds
    //endregion

    //region UI  variables
    TextView question1;
    TextView question2;
    TextView question3;
    TextView question4;
    TextView question5;
    TextView question6;
    TextView question7;
    TextView question8;
    TextView question9;

    SeekBar answer1;
    SeekBar answer2;
    SeekBar answer3;
    SeekBar answer4;
    SeekBar answer5;
    SeekBar answer6;
    SeekBar answer7;
    SeekBar answer8;
    SeekBar answer9;

    Button btnSubmit;
    //endregion

    DatabaseHelper db;
    private short emaOrder;

    private SharedPreferences loginPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginPrefs = getSharedPreferences("UserLogin", MODE_PRIVATE);
        setContentView(R.layout.activity_ema);
        db = new DatabaseHelper(this);
        init();
    }

    public void init() {
        question1 = findViewById(R.id.question1);
        question2 = findViewById(R.id.question2);
        question3 = findViewById(R.id.question3);
        question4 = findViewById(R.id.question4);
        question5 = findViewById(R.id.question5);
        question6 = findViewById(R.id.question6);
        question7 = findViewById(R.id.question7);
        question8 = findViewById(R.id.question8);
        question9 = findViewById(R.id.question9);

        answer1 = findViewById(R.id.scale_q1);
        answer2 = findViewById(R.id.scale_q2);
        answer3 = findViewById(R.id.scale_q3);
        answer4 = findViewById(R.id.scale_q4);
        answer5 = findViewById(R.id.scale_q5);
        answer6 = findViewById(R.id.scale_q6);
        answer7 = findViewById(R.id.scale_q7);
        answer8 = findViewById(R.id.scale_q8);
        answer9 = findViewById(R.id.scale_q9);

        btnSubmit = findViewById(R.id.btn_submit);

        //emaResponses = new EmaResponses();
        //current_question = 1;
        emaOrder = getIntent().getShortExtra("notifOrder", (short) -1);
        Log.e(TAG, "EMA order: " + emaOrder);

        //prepareViewForQuestion(current_question);
    }

    public void clickSubmit(View view) {

        long timestamp = System.currentTimeMillis();

        if (Tools.isNetworkAvailable(this))
            Tools.execute(new MyRunnable(
                    this,
                    getString(R.string.url_ema_submit, getString(R.string.server_ip)),
                    loginPrefs.getString(SignInActivity.user_id, null),
                    loginPrefs.getString(SignInActivity.password, null),
                    timestamp
            ) {
                @Override
                public void run() {
                    String url = (String) args[0];
                    String email = (String) args[1];
                    String password = (String) args[2];
                    long timestamp = (long) args[3];
                    try {
                        JSONObject body = new JSONObject();
                        body.put("username", email);
                        body.put("password", password);
                        body.put("ema_timestamp", timestamp);
                        body.put("ema_order", emaOrder);
                        String answers = String.format(Locale.US, "%d %d %d %d %d %d %d %d %d",
                                answer1.getProgress() + 1,
                                answer2.getProgress() + 1,
                                answer3.getProgress() + 1,
                                answer4.getProgress() + 1,
                                answer5.getProgress() + 1,
                                answer6.getProgress() + 1,
                                answer7.getProgress() + 1,
                                answer8.getProgress() + 1,
                                answer9.getProgress() + 1);
                        body.put("answers", answers);

                        Log.e(TAG, "EMA: " + body.toString());
                        JSONObject json = new JSONObject(Tools.post(url, body));
                        switch (json.getInt("result")) {
                            case Tools.RES_OK:
                                runOnUiThread(new MyRunnable(activity, args) {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "Submitted to Server", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                                break;
                            case Tools.RES_FAIL:
                                Thread.sleep(2000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "Failed to submit to server", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case Tools.RES_SRV_ERR:
                                Thread.sleep(2000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "Failed to submit. (SERVER SIDE ERROR)", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                    } catch (IOException | JSONException | InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, "Failed to submit to server", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    enableTouch();
                }
            });
        else {
            Log.d(TAG, "No connection case");
            String answers = String.format(Locale.US, "%d %d %d %d %d %d %d %d %d",
                    answer1.getProgress() + 1,
                    answer2.getProgress() + 1,
                    answer3.getProgress() + 1,
                    answer4.getProgress() + 1,
                    answer5.getProgress() + 1,
                    answer6.getProgress() + 1,
                    answer7.getProgress() + 1,
                    answer8.getProgress() + 1,
                    answer9.getProgress() + 1);

            boolean isInserted = db.insertEMAData(emaOrder, timestamp, answers);
            if (isInserted) {
                Toast.makeText(this, "Response saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else
                Log.d(TAG, "Failed to save");

        }

        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.putBoolean("ema_btn_make_visible", false);
        editor.apply();

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(EMA_NOTIFICATION_ID);

        /*
        SharedPreferences.Editor editor = Tools.loginPrefs.edit();
        editor.putLong("ema_btn_set_visible", 0);
        editor.putInt("notifOrder", -1);
        editor.apply();*/
    }
}
