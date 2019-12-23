package kr.ac.inha.nsl;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;

import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import static kr.ac.inha.nsl.LocationsSettingActivity.GEOFENCE_RADIUS_DEFAULT;
import static kr.ac.inha.nsl.MainActivity.PERMISSIONS;

public class SignInActivity extends Activity {

    // region Variables
    public static final String TAG = "SignInActivity";
    private EditText userID;
    private EditText userPassword;
    private RelativeLayout loadingPanel;
    private SharedPreferences loginPrefs;

    public static final String user_id = "id", password = "password";
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_sign_in);


        // region Initialize UI Variables
        userID = findViewById(R.id.txt_userID);
        userPassword = findViewById(R.id.txt_password);
        loadingPanel = findViewById(R.id.loadingPanel);
        // endregion
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginPrefs = getApplicationContext().getSharedPreferences("UserLogin", MODE_PRIVATE);
        if (!Tools.hasPermissions(this, PERMISSIONS)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permissions")
                    .setMessage("You have to grant permissions first!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Tools.grantPermissions(SignInActivity.this, PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
        } else {
            if (loginPrefs.getBoolean("logged_in", false)) {
                Intent intent;
                if (getIntent().getBooleanExtra("forEMA", false)) {
                    intent = new Intent(SignInActivity.this, EMAActivity.class);
                    intent.putExtra("notifOrder", getIntent().getShortExtra("notifOrder", (short) -1));
                    Log.e(TAG, "notifOrder: " + getIntent().getShortExtra("notifOrder", (short) -1));
                    startActivity(intent);
                    finish();
                } else if (getIntent().getBooleanExtra("fromReboot", false)) {
                    resetGeofences();
                    intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else Toast.makeText(this, "No log in yet", Toast.LENGTH_SHORT).show();
        }
    }

    public void signInClick(View view) {
        loadingPanel.setVisibility(View.VISIBLE);

        if (Tools.isNetworkAvailable(this))
            Tools.execute(new MyRunnable(
                    this,
                    getString(R.string.url_user_login, getString(R.string.server_ip)),
                    userID.getText().toString(),
                    userPassword.getText().toString()
            ) {
                @Override
                public void run() {
                    String url = (String) args[0];
                    String id = (String) args[1];
                    String password = (String) args[2];
                    try {
                        JSONObject body = new JSONObject();
                        body.put("username", id);
                        body.put("password", password);

                        JSONObject json = new JSONObject(Tools.post(url, body));
                        switch (json.getInt("result")) {
                            case Tools.RES_OK:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        SharedPreferences.Editor editor = loginPrefs.edit();
                                        editor.putString(SignInActivity.user_id, userID.getText().toString());
                                        editor.putString(SignInActivity.password, userPassword.getText().toString());
                                        editor.apply();


                                        if (!loginPrefs.getBoolean("logged_in", false)) {
                                            resetGeofences();
                                        }
                                        editor.putBoolean("logged_in", true);
                                        editor.apply();

                                        loadingPanel.setVisibility(View.GONE);
                                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                });
                                break;
                            case Tools.RES_FAIL:
                                Thread.sleep(2000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SignInActivity.this, "Failed to sign in.", Toast.LENGTH_SHORT).show();
                                        loadingPanel.setVisibility(View.GONE);
                                    }
                                });
                                break;
                            case Tools.RES_SRV_ERR:
                                Thread.sleep(2000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SignInActivity.this, "Failed to sign in. (SERVER SIDE ERROR)", Toast.LENGTH_SHORT).show();
                                        loadingPanel.setVisibility(View.GONE);
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
                                Toast.makeText(SignInActivity.this, "Failed to sign in.", Toast.LENGTH_SHORT).show();
                                loadingPanel.setVisibility(View.GONE);
                            }
                        });
                    }
                    enableTouch();
                }
            });

        else {
            Toast.makeText(this, "Internet is not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void signUpClick(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void resetGeofences() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        for (LocationsSettingActivity.StoreLocation location : LocationsSettingActivity.ALL_LOCATIONS) {
            if (LocationsSettingActivity.getLocationData(getApplicationContext(), location) != null) {
                GeofenceHelper.startGeofence(this,
                        Objects.requireNonNull(LocationsSettingActivity.getLocationData(getApplicationContext(), location)).getmId(),
                        Objects.requireNonNull(LocationsSettingActivity.getLocationData(getApplicationContext(), location)).getmLatLng(),
                        GEOFENCE_RADIUS_DEFAULT);
                Log.e(TAG, "Geofences are reset");
            } else
                Log.e(TAG, "No Geofences in shared preferences");
        }
    }

    @Override
    protected void onStop() {
        loadingPanel.setVisibility(View.GONE);
        super.onStop();
    }
}
