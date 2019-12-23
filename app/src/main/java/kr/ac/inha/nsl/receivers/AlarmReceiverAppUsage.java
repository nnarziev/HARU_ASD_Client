package kr.ac.inha.nsl.receivers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import kr.ac.inha.nsl.DatabaseHelper;
import kr.ac.inha.nsl.Tools;

import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_APP_USAGE;

public class AlarmReceiverAppUsage extends BroadcastReceiver {
    public static final String TAG = "AlarmReceiverAppUsage";

    @Override
    public void onReceive(Context con, Intent intent) {
        if (Tools.isMainServiceRunning(con)) {
            DatabaseHelper db = new DatabaseHelper(con);
            PackageManager mPm = con.getPackageManager();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, -24);

            UsageStatsManager usageStatsManager = (UsageStatsManager) con.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.getTimeInMillis(), Calendar.getInstance().getTimeInMillis());
            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                UsageStats pkgStats = stats.get(i);
                // load application labels for each application
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    String pkg_name = pkgStats.getPackageName();
                    String app_name = (String) mPm.getApplicationLabel(appInfo);
                    long duration = pkgStats.getTotalTimeInForeground() / 1000;

                    if (duration > 0) {
                        String value = pkgStats.getFirstTimeStamp() + "||" + pkg_name + "||" + app_name + "||" + duration;
                        db.insertSensorData(DATA_SRC_APP_USAGE, value);
                        Log.e(TAG, app_name + " -> first time: " + pkgStats.getFirstTimeStamp() + ";  last time: " + pkgStats.getLastTimeStamp() + ";  total (sec): " + duration);
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
