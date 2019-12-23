package kr.ac.inha.nsl.old;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import java.util.HashMap;

import kr.ac.inha.nsl.DatabaseHelper;
import kr.ac.inha.nsl.R;


public class DbViewActivity extends AppCompatActivity {

    private WebView wvDbTable;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_view);
        db = new DatabaseHelper(this);
        wvDbTable = findViewById(R.id.wvDbTable);
        wvDbTable.setMinimumWidth(2000);
        findViewById(R.id.btnRefreshWindow).callOnClick();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    public void refreshWindowClick(View view) {
        StringBuilder sb = new StringBuilder("<!DOCTYPE html><html><style type=\"text/css\">table {border-collapse: collapse;} th,td{padding: 5px; white-space: nowrap;}</style><body><table border=\"1\"><thead><th> # </th><th>SENSOR_ID</th><th>TIMESTAMP</th><th>ACCURACY</th><th>RAW DATA(HEX)</th></thead><tbody>");

        /*Cursor cursor = null;
        if (getIntent().getShortExtra("ViewDBFor", (short) 0) == DatabaseHelper.SM_PHONE)
            cursor = db.getAllSensorData(DatabaseHelper.SM_PHONE);
        else if (getIntent().getShortExtra("ViewDBFor", (short) 0) == DatabaseHelper.SM_WATCH)
            cursor = db.getAllSensorData(DatabaseHelper.SM_WATCH);

        int i = 0;
        if (cursor.moveToFirst())
            do {
                byte sensorId = (byte) cursor.getShort(0);
                long timestamp = cursor.getLong(1);
                int accuracy = cursor.getInt(2);
                byte[] data = cursor.getBlob(3);

                sb.append("<tr><td>");
                sb.append(i);
                i++;
                sb.append("</td><td>");
                sb.append(sensorId);
                sb.append("</td><td>");
                sb.append(timestamp);
                sb.append("</td><td>");
                sb.append(accuracy);
                sb.append("</td><td>");
                sb.append(Tools.bytes2hexString(data));
                sb.append("</td></tr>");

                final int sens = (int) sensorId;
                switch (sens) {
                    case 1:
                        sensorVal_1++;
                        break;
                    case 2:
                        sensorVal_2++;
                        break;
                    case 4:
                        sensorVal_4++;
                        break;
                    case 5:
                        sensorVal_5++;
                        break;
                    case 6:
                        sensorVal_6++;
                        break;
                    case 8:
                        sensorVal_8++;
                        break;
                    case 9:
                        sensorVal_9++;
                        break;
                    case 10:
                        sensorVal_10++;
                        break;
                    case 11:
                        sensorVal_11++;
                        break;
                    case 14:
                        sensorVal_14++;
                        break;
                    case 15:
                        sensorVal_15++;
                        break;
                    case 16:
                        sensorVal_16++;
                        break;
                    case 18:
                        sensorVal_18++;
                        break;
                    case 19:
                        sensorVal_19++;
                        break;
                    case 20:
                        sensorVal_20++;
                        break;
                    default:
                        break;


                }
            } while (cursor.moveToNext());

        sb.append("</tbody></table></body><html>");
        cursor.close();*/
        wvDbTable.loadData(sb.toString(), "text/html", "UTF-8");

        Log.e("SENSORS", "SENSOR 1: " + sensorVal_1);
        Log.e("SENSORS", "SENSOR 2: " + sensorVal_2);
        Log.e("SENSORS", "SENSOR 3: " + sensorVal_3);
        Log.e("SENSORS", "SENSOR 4: " + sensorVal_4);
        Log.e("SENSORS", "SENSOR 5: " + sensorVal_5);
        Log.e("SENSORS", "SENSOR 6: " + sensorVal_6);
        Log.e("SENSORS", "SENSOR 7: " + sensorVal_7);
        Log.e("SENSORS", "SENSOR 8: " + sensorVal_8);
        Log.e("SENSORS", "SENSOR 9: " + sensorVal_9);
        Log.e("SENSORS", "SENSOR 10: " + sensorVal_10);
        Log.e("SENSORS", "SENSOR 11: " + sensorVal_11);
        Log.e("SENSORS", "SENSOR 12: " + sensorVal_12);
        Log.e("SENSORS", "SENSOR 13: " + sensorVal_13);
        Log.e("SENSORS", "SENSOR 14: " + sensorVal_14);
        Log.e("SENSORS", "SENSOR 15: " + sensorVal_15);
        Log.e("SENSORS", "SENSOR 16: " + sensorVal_16);
        Log.e("SENSORS", "SENSOR 17: " + sensorVal_17);
        Log.e("SENSORS", "SENSOR 18: " + sensorVal_18);
        Log.e("SENSORS", "SENSOR 19: " + sensorVal_19);
        Log.e("SENSORS", "SENSOR 20: " + sensorVal_20);
    }

    HashMap<Integer, Integer> sensorValMap = new HashMap<>();


    int sensorVal_1 = 0;
    int sensorVal_2 = 0;
    int sensorVal_3 = 0;
    int sensorVal_4 = 0;
    int sensorVal_5 = 0;
    int sensorVal_6 = 0;
    int sensorVal_7 = 0;
    int sensorVal_8 = 0;
    int sensorVal_9 = 0;
    int sensorVal_10 = 0;
    int sensorVal_11 = 0;
    int sensorVal_12 = 0;
    int sensorVal_13 = 0;
    int sensorVal_14 = 0;
    int sensorVal_15 = 0;
    int sensorVal_16 = 0;
    int sensorVal_17 = 0;
    int sensorVal_18 = 0;
    int sensorVal_19 = 0;
    int sensorVal_20 = 0;
    int sensorVal_21 = 0;

}
