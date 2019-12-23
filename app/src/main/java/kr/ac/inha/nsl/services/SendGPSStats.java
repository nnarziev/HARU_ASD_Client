package kr.ac.inha.nsl.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import kr.ac.inha.nsl.DatabaseHelper;

import static kr.ac.inha.nsl.LocationsSettingActivity.ID_HOME;
import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_MAX_DIST_FROM_HOME;
import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_MAX_DIST_TWO_LOCATIONS;
import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_NUM_OF_DIF_PLACES;
import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_RADIUS_OF_GYRATION;
import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_STDDEV_OF_DISPLACEMENT;
import static kr.ac.inha.nsl.services.CustomSensorsService.DATA_SRC_TOTAL_DIST_COVERED;
import static kr.ac.inha.nsl.services.CustomSensorsService.SERVICE_START_X_MIN_BEFORE_EMA;
import static kr.ac.inha.nsl.services.LocationService.LOCATIONS_TXT;

public class SendGPSStats extends IntentService {
    public static final String TAG = "SendGPSStats";
    private ArrayList<LatLng> mLocationsList = new ArrayList<>();
    private ArrayList<Long> mLocationsTimestamps = new ArrayList<>();

    public SendGPSStats() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            DatabaseHelper db = new DatabaseHelper(this);
            long time_end = System.currentTimeMillis();
            long time_start = time_end - SERVICE_START_X_MIN_BEFORE_EMA * 60 * 1000;
            short emaOrder = intent.getShortExtra("notifOrder", (short) -1);
            try {
                Log.e(TAG, "EMA " + emaOrder + "-based GPS statistics");
                if (readLocations(time_start, time_end) > 1) {
                    db.insertSensorData(DATA_SRC_TOTAL_DIST_COVERED, time_start + " " + time_end + " " + getTotalDisCovered() + " " + emaOrder);
                    db.insertSensorData(DATA_SRC_MAX_DIST_FROM_HOME, time_start + " " + time_end + " " + getMaxDistFromHome() + " " + emaOrder);
                    db.insertSensorData(DATA_SRC_MAX_DIST_TWO_LOCATIONS, time_start + " " + time_end + " " + getMaxDistBtwTwoLocations() + " " + emaOrder);
                    db.insertSensorData(DATA_SRC_RADIUS_OF_GYRATION, time_start + " " + time_end + " " + getRadiusOfGyration() + " " + emaOrder);
                    db.insertSensorData(DATA_SRC_STDDEV_OF_DISPLACEMENT, time_start + " " + time_end + " " + getStdDevOfDisplacement() + " " + emaOrder);
                    db.insertSensorData(DATA_SRC_NUM_OF_DIF_PLACES, time_start + " " + time_end + " " + NumOfDifPlaces() + " " + emaOrder);

                    Log.e(TAG, "1) Total distance covered: " + getTotalDisCovered());
                    Log.e(TAG, "2) Max dist from home: " + getMaxDistFromHome());
                    Log.e(TAG, "3) Max dist btw two locations: " + getMaxDistBtwTwoLocations());
                    Log.e(TAG, "4) Radius of gyration: " + getRadiusOfGyration());
                    Log.e(TAG, "5) Std deviation of displacement: " + getStdDevOfDisplacement());
                    Log.e(TAG, "6) Number of different places: " + NumOfDifPlaces());
                } else
                    Log.e(TAG, "No locations yet");


                if (emaOrder == 4) {
                    time_start = time_end - 24 * 60 * 60 * 1000; //for every day statistics
                    Log.e(TAG, "Daily-based GPS statistics");
                    if (readLocations(time_start, time_end) > 1) {
                        db.insertSensorData(DATA_SRC_TOTAL_DIST_COVERED, time_start + " " + time_end + " " + getTotalDisCovered() + " " + 0);
                        db.insertSensorData(DATA_SRC_MAX_DIST_FROM_HOME, time_start + " " + time_end + " " + getMaxDistFromHome() + " " + 0);
                        db.insertSensorData(DATA_SRC_MAX_DIST_TWO_LOCATIONS, time_start + " " + time_end + " " + getMaxDistBtwTwoLocations() + " " + 0);
                        db.insertSensorData(DATA_SRC_RADIUS_OF_GYRATION, time_start + " " + time_end + " " + getRadiusOfGyration() + " " + 0);
                        db.insertSensorData(DATA_SRC_STDDEV_OF_DISPLACEMENT, time_start + " " + time_end + " " + getStdDevOfDisplacement() + " " + 0);
                        db.insertSensorData(DATA_SRC_NUM_OF_DIF_PLACES, time_start + " " + time_end + " " + NumOfDifPlaces() + " " + 0);

                        Log.e(TAG, "1) Total distance covered: " + getTotalDisCovered());
                        Log.e(TAG, "2) Max dist from home: " + getMaxDistFromHome());
                        Log.e(TAG, "3) Max dist btw two locations: " + getMaxDistBtwTwoLocations());
                        Log.e(TAG, "4) Radius of gyration: " + getRadiusOfGyration());
                        Log.e(TAG, "5) Std deviation of displacement: " + getStdDevOfDisplacement());
                        Log.e(TAG, "6) Number of different places: " + NumOfDifPlaces());
                    } else
                        Log.e(TAG, "No locations yet");
                }
            } catch (Exception e) {
                Log.e(TAG, "No such file exception");
            }
        }
    }

    private int readLocations(long start, long end) throws IOException {
        FileInputStream fis = this.openFileInput(LOCATIONS_TXT);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] tokens = line.split(",");
            long timestamp = Long.parseLong(tokens[0]);
            if (start <= timestamp && timestamp <= end) {
                double lat = Double.parseDouble(tokens[1]);
                double lng = Double.parseDouble(tokens[2]);
                mLocationsList.add(new LatLng(lat, lng));
                mLocationsTimestamps.add(timestamp);
            }
        }
        return mLocationsList.size();
    }

    private boolean removeLocationsFile() {
        String path = this.getFilesDir() + "/" + LOCATIONS_TXT;
        File file = new File(path);
        return file.delete();
    }

    private float getTotalDisCovered() {
        //returns 0 if "locations.txt" doesn't exist
        float total_dist_res = 0;
        if (mLocationsList.size() > 0) {
            float[] results = new float[5];
            for (int i = 0; i < mLocationsList.size() - 1; i++) {
                LatLng first = mLocationsList.get(i);
                LatLng second = mLocationsList.get(i + 1);
                Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, results);
                float distance = results[0];
                total_dist_res += distance;
            }
        }
        return total_dist_res;
    }

    private float getMaxDistBtwTwoLocations() {
        //returns 0 if "locations.txt" doesn't exist
        float max_dist_two_locations = 0;
        if (mLocationsList.size() > 0) {
            float[] results = new float[5];
            for (int i = 0; i < mLocationsList.size() - 1; i++) {
                LatLng first = mLocationsList.get(i);
                LatLng second = mLocationsList.get(i + 1);
                Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, results);
                float distance = results[0];
                if (distance > max_dist_two_locations)
                    max_dist_two_locations = distance;
            }
        }


        return max_dist_two_locations;
    }

    private float getMaxDistFromHome() {
        SharedPreferences locationPrefs = getSharedPreferences("UserLocations", MODE_PRIVATE);
        //returns 0 if home locations is not set or "locations.txt" doesn't exist
        float max_dist_from_home = 0;
        if (mLocationsList.size() > 0) {
            float home_lat = locationPrefs.getFloat(ID_HOME + "_LAT", 0);
            float home_lng = locationPrefs.getFloat(ID_HOME + "_LNG", 0);
            if (home_lat == 0 || home_lng == 0) // case when home locations is not set
                return max_dist_from_home;
            float[] results = new float[5];
            for (LatLng location : mLocationsList) {
                Location.distanceBetween(home_lat, home_lng, location.latitude, location.longitude, results);
                float distance = results[0];
                if (distance > max_dist_from_home) {
                    max_dist_from_home = distance;
                }
            }
        }
        return max_dist_from_home;
    }

    private float getRadiusOfGyration() {
        float radius_of_gyration = 0;
        if (mLocationsList.size() > 1) {
            double[] centroid = {0.0, 0.0};
            int total_time_in_locations = 0;

            for (int i = 0; i < mLocationsList.size() - 1; i++) {
                total_time_in_locations += (int) ((mLocationsTimestamps.get(i + 1) - mLocationsTimestamps.get(i)) / 1000);
                centroid[0] += mLocationsList.get(i).latitude;
                centroid[1] += mLocationsList.get(i).longitude;
            }

            centroid[0] = centroid[0] / (mLocationsList.size() - 1);
            centroid[1] = centroid[1] / (mLocationsList.size() - 1);

            int sum = 0;
            for (int i = 0; i < mLocationsList.size() - 1; i++) {
                int time_spent = (int) ((mLocationsTimestamps.get(i + 1) - mLocationsTimestamps.get(i)) / 1000);
                float[] dist_result = new float[5];
                Location.distanceBetween(mLocationsList.get(i).latitude, mLocationsList.get(i).longitude, centroid[0], centroid[1], dist_result);
                sum += time_spent * Math.pow(dist_result[0], 2);
            }

            radius_of_gyration = (float) Math.sqrt(sum / total_time_in_locations);
        }
        return radius_of_gyration;
    }

    private float getStdDevOfDisplacement() {
        float std_dev_of_disp = 0;
        if (mLocationsList.size() > 1) {
            float total_distance = 0;
            for (int i = 0; i < mLocationsList.size() - 1; i++) {
                float[] dist_result = new float[5];
                Location.distanceBetween(mLocationsList.get(i).latitude, mLocationsList.get(i).longitude, mLocationsList.get(i + 1).latitude, mLocationsList.get(i + 1).longitude, dist_result);
                total_distance += dist_result[0];
            }

            float avg_displacementavg_displacement = total_distance / mLocationsList.size() - 1;

            float sum = 0;
            for (int i = 0; i < mLocationsList.size() - 1; i++) {
                float[] dist_result = new float[5];
                Location.distanceBetween(mLocationsList.get(i).latitude, mLocationsList.get(i).longitude, mLocationsList.get(i + 1).latitude, mLocationsList.get(i + 1).longitude, dist_result);
                sum += Math.pow(dist_result[0] - avg_displacementavg_displacement, 2);
            }

            std_dev_of_disp = (float) Math.sqrt(sum / (mLocationsList.size() - 1));
        }
        return std_dev_of_disp;
    }

    private float NumOfDifPlaces() {
        float num_of_dif_places = 0;
        if (mLocationsList.size() > 0)
            num_of_dif_places = mLocationsList.size();
        return num_of_dif_places;
    }
}
