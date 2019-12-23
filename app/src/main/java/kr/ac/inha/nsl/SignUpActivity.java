package kr.ac.inha.nsl;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();
    }

    // region Variables
    private EditText name;
    private EditText userID;
    private EditText phone;
    private EditText password;
    private EditText confPassword;
    // endregion

    private void init() {
        // region Initialize UI Variables
        name = findViewById(R.id.txt_name);
        userID = findViewById(R.id.txt_userID);
        phone = findViewById(R.id.txt_phone_num);
        password = findViewById(R.id.txt_password);
        confPassword = findViewById(R.id.txt_conf_password);
        // endregion
    }

    public void userRegister(String name, String userID, String phone, String password) {
        Tools.execute(new MyRunnable(
                this,
                getString(R.string.url_user_register, getString(R.string.server_ip)),
                name,
                userID,
                phone,
                password
        ) {
            @Override
            public void run() {
                String url = (String) args[0];
                String name = (String) args[1];
                String userID = (String) args[2];
                String phone = (String) args[3];
                String password = (String) args[4];

                try {
                    JSONObject body = new JSONObject();
                    body.put("name", name);
                    body.put("username", userID);
                    body.put("phone_num", phone);
                    body.put("password", password);
                    body.put("device_info", Build.BRAND + ", " + Build.MODEL + ", " + Build.VERSION.RELEASE);


                    JSONObject json = new JSONObject(Tools.post(url, body));

                    Log.e("JSON", "Result: " + json.toString());
                    switch (json.getInt("result")) {
                        case Tools.RES_OK:
                            runOnUiThread(new MyRunnable(activity, args) {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUpActivity.this, "Successfully signed up. You can sign in now!", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                }
                            });
                            break;
                        case Tools.RES_FAIL:
                            Thread.sleep(2000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUpActivity.this, "Username already exists, please try another username!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case Tools.RES_SRV_ERR:
                            Thread.sleep(2000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUpActivity.this, "Failed to sign up. (SERVER SIDE ERROR)", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        default:
                            break;
                    }
                } catch (JSONException | InterruptedException | IOException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignUpActivity.this, "Failed to sign up.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                enableTouch();
            }
        });
    }

    public void registerClick(View view) {
        String usrName = name.getText().toString();
        String usrID = userID.getText().toString();
        String phoneNum = phone.getText().toString();
        String usrPassword = password.getText().toString();
        String usrConfirmPass = confPassword.getText().toString();

        if (isRegistrationValid(usrName, usrID, phoneNum, usrPassword, usrConfirmPass))
            userRegister(usrName, usrID, phoneNum, usrPassword);
        else
            Toast.makeText(this, "Invalid input. Please recheck inputs and try again!", Toast.LENGTH_SHORT).show();
    }

    public boolean isRegistrationValid(String usrName, String usrID, String phoneNum, String password, String confirmPass) {

        return usrName != null &&
                usrID != null &&
                phoneNum != null &&
                password != null &&
                password.length() >= 6 &&
                password.length() <= 16 &&
                password.equals(confirmPass);
    }
}
