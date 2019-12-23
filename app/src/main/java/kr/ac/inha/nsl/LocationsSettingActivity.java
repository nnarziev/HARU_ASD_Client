package kr.ac.inha.nsl;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.app.AlertDialog;
import android.location.Location;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.location.LocationListener;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.GoogleMap;

import android.graphics.drawable.BitmapDrawable;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Objects;


public class LocationsSettingActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    //region Constants
    public static final String TAG = "LocationsSetting";
    public static final String ID_HOME = "HOME";
    public static final String ID_DORM = "DORM";
    public static final String ID_UNIV = "UNIV";
    public static final String ID_LIBRARY = "LIBRARY";
    public static final String ID_ADDITIONAL = "ADDITIONAL";

    private String TITLE_HOME;
    private String TITLE_DORM;
    private String TITLE_UNIV;
    private String TITLE_LIBRARY;
    private String TITLE_ADDITIONAL;

    public static final int GEOFENCE_RADIUS_DEFAULT = 100;
    static final StoreLocation[] ALL_LOCATIONS = new StoreLocation[]
            {
                    new StoreLocation(ID_HOME, null),
                    new StoreLocation(ID_DORM, null),
                    new StoreLocation(ID_UNIV, null),
                    new StoreLocation(ID_LIBRARY, null),
                    new StoreLocation(ID_ADDITIONAL, null)
            };

    //endregion

    private GoogleMap mMap;
    private Marker currentGeofenceMarker;
    private StoreLocation currentStoringLocation;
    private Circle geoLimits;

    //region Internal classes
    static class StoreLocation {
        LatLng mLatLng;
        String mId;
        int rad;

        StoreLocation(String id, LatLng latlng/*, int radius*/) {
            mLatLng = latlng;
            mId = id;
            //rad = radius;
        }

        LatLng getmLatLng() {
            return mLatLng;
        }

        String getmId() {
            return mId;
        }

        int getRadius() {
            return rad;
        }

        void setRadius(int radius) {
            rad = radius;
        }
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_setting);

        TITLE_HOME = getString(R.string.set_home_location);
        TITLE_DORM = getString(R.string.set_dorm_location);
        TITLE_UNIV = getString(R.string.set_university_location);
        TITLE_LIBRARY = getString(R.string.set_library_location);
        TITLE_ADDITIONAL = getString(R.string.set_additional_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

        for (StoreLocation location : ALL_LOCATIONS) {
            if (getLocationData(getApplicationContext(), location) != null) {
                addLocationMarker(Objects.requireNonNull(getLocationData(getApplicationContext(), location)));
            }
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 20, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        markerForGeofence(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        removeLocation(marker.getTitle());
        drawGeofence(marker, Integer.valueOf(marker.getSnippet()));
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        markerForGeofence(latLng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void drawGeofence(Marker marker, int radius) {
        if (geoLimits != null) {
            geoLimits.remove();
        }

        CircleOptions circleOptions = new CircleOptions()
                .center(marker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .strokeWidth(1)
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(radius);

        geoLimits = mMap.addCircle(circleOptions);
    }

    private void markerForGeofence(LatLng latLng) {
        MarkerOptions optionsMarker = new MarkerOptions()
                .snippet(String.valueOf(GEOFENCE_RADIUS_DEFAULT))
                .position(latLng)
                .title("Current Location");
        if (mMap != null) {
            if (currentGeofenceMarker != null) {
                currentGeofenceMarker.remove();
            }
            currentGeofenceMarker = mMap.addMarker(optionsMarker);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            drawGeofence(currentGeofenceMarker, GEOFENCE_RADIUS_DEFAULT);
        }
    }

    private void addLocationMarker(StoreLocation location) {
        Drawable iconDrawable;
        String location_title;
        switch (location.getmId()) {
            case ID_HOME:
                iconDrawable = getResources().getDrawable(R.drawable.home);
                location_title = TITLE_HOME;
                break;
            case ID_DORM:
                iconDrawable = getResources().getDrawable(R.drawable.dormitory);
                location_title = TITLE_DORM;
                break;
            case ID_UNIV:
                iconDrawable = getResources().getDrawable(R.drawable.university);
                location_title = TITLE_UNIV;
                break;
            case ID_LIBRARY:
                iconDrawable = getResources().getDrawable(R.drawable.library);
                location_title = TITLE_LIBRARY;
                break;
            case ID_ADDITIONAL:
                iconDrawable = getResources().getDrawable(R.drawable.additional);
                location_title = TITLE_ADDITIONAL;
                break;
            default:
                iconDrawable = getResources().getDrawable(R.mipmap.ic_launcher_no_bg);
                location_title = "My location";
                break;
        }

        Bitmap iconBmp = ((BitmapDrawable) iconDrawable).getBitmap();
        mMap.addMarker(new MarkerOptions()
                .title(location_title)
                .snippet(String.valueOf(GEOFENCE_RADIUS_DEFAULT))
                .position(location.getmLatLng())
                .icon(BitmapDescriptorFactory.fromBitmap(iconBmp)));
    }

    static StoreLocation getLocationData(Context con, StoreLocation location) {
        SharedPreferences locationPrefs = con.getSharedPreferences("UserLocations", MODE_PRIVATE);
        float lat = locationPrefs.getFloat(location.getmId() + "_LAT", 0);
        float lng = locationPrefs.getFloat(location.getmId() + "_LNG", 0);
        if (lat != 0 && lng != 0) {
            return new StoreLocation(location.getmId(), new LatLng(lat, lng));
        }
        return null;
    }

    private void setLocation(String locationText, StoreLocation location) {
        final SharedPreferences locationPrefs = getSharedPreferences("UserLocations", MODE_PRIVATE);
        SharedPreferences.Editor editor = locationPrefs.edit();
        editor.putFloat(location.getmId() + "_LAT", (float) location.getmLatLng().latitude);
        editor.putFloat(location.getmId() + "_LNG", (float) location.getmLatLng().longitude);
        editor.apply();
        Toast.makeText(getApplicationContext(), locationText + " 위치를 설정했습니다!", Toast.LENGTH_SHORT).show();
        currentStoringLocation.setRadius(GEOFENCE_RADIUS_DEFAULT);

        String location_id = currentStoringLocation.getmId();
        LatLng position = currentStoringLocation.getmLatLng();
        int radius = currentStoringLocation.getRadius();

        GeofenceHelper.startGeofence(getApplicationContext(), location_id, position, radius);

        onMapReady(mMap);
        drawGeofence(currentGeofenceMarker, currentStoringLocation.getRadius());
    }

    private void removeLocation(String markerTitle) {
        if (markerTitle.equals(TITLE_HOME))
            displayRemoveDialog(ID_HOME);
        else if (markerTitle.equals(TITLE_DORM))
            displayRemoveDialog(ID_DORM);
        else if (markerTitle.equals(TITLE_UNIV))
            displayRemoveDialog(ID_UNIV);
        else if (markerTitle.equals(TITLE_LIBRARY))
            displayRemoveDialog(ID_LIBRARY);
        else if (markerTitle.equals(TITLE_ADDITIONAL))
            displayRemoveDialog(ID_ADDITIONAL);
    }

    public void displayRemoveDialog(final String locationId) {

        final SharedPreferences locationPrefs = getSharedPreferences("UserLocations", MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("주의!");
        builder.setMessage("저장된 위치를 해제하시겠습니까?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GeofenceHelper.removeGeofence(getApplicationContext(), locationId);
                SharedPreferences.Editor editor = locationPrefs.edit();
                editor.remove(locationId + "_LAT");
                editor.remove(locationId + "_LNG");
                editor.remove(locationId + "_ENTERED_TIME");
                editor.apply();
                Toast.makeText(LocationsSettingActivity.this, "저장된 위치가 해제되었습니다!", Toast.LENGTH_SHORT).show();
                onMapReady(mMap);
                drawGeofence(currentGeofenceMarker, GEOFENCE_RADIUS_DEFAULT);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    //region Buttons click listeners
    public void setHomeClick(View view) {
        currentStoringLocation = new StoreLocation(ID_HOME, new LatLng(currentGeofenceMarker.getPosition().latitude, currentGeofenceMarker.getPosition().longitude));
        setLocation(TITLE_HOME, currentStoringLocation);
    }

    public void setDormClick(View view) {
        currentStoringLocation = new StoreLocation(ID_DORM, new LatLng(currentGeofenceMarker.getPosition().latitude, currentGeofenceMarker.getPosition().longitude));
        setLocation(TITLE_DORM, currentStoringLocation);
    }

    public void setUnivClick(View view) {
        currentStoringLocation = new StoreLocation(ID_UNIV, new LatLng(currentGeofenceMarker.getPosition().latitude, currentGeofenceMarker.getPosition().longitude));
        setLocation(TITLE_UNIV, currentStoringLocation);
    }

    public void setLibraryClick(View view) {
        currentStoringLocation = new StoreLocation(ID_LIBRARY, new LatLng(currentGeofenceMarker.getPosition().latitude, currentGeofenceMarker.getPosition().longitude));
        setLocation(TITLE_LIBRARY, currentStoringLocation);
    }

    public void setAdditionalPlaceClick(View view) {
        currentStoringLocation = new StoreLocation(ID_ADDITIONAL, new LatLng(currentGeofenceMarker.getPosition().latitude, currentGeofenceMarker.getPosition().longitude));
        setLocation(TITLE_ADDITIONAL, currentStoringLocation);
    }
    //endregion

    public void displayDialog(final String locationText, final StoreLocation location) {
        final SharedPreferences locationPrefs = getSharedPreferences("UserLocations", MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation!");
        builder.setMessage("Are you sure it's your " + locationText + "? If it's then please, specify the radius below.");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = locationPrefs.edit();
                editor.putFloat(location.getmId() + "_LAT", (float) location.getmLatLng().latitude);
                editor.putFloat(location.getmId() + "_LNG", (float) location.getmLatLng().longitude);
                int tmp_radius = GEOFENCE_RADIUS_DEFAULT;
                if (!input.getText().toString().equals(""))
                    tmp_radius = Integer.valueOf(input.getText().toString());
                editor.putInt(location.getmId() + "_RADIUS", tmp_radius);
                editor.apply();
                Toast.makeText(getApplicationContext(), locationText + " is set :)", Toast.LENGTH_SHORT).show();
                currentStoringLocation.setRadius(Integer.valueOf(String.valueOf(tmp_radius)));

                String location_id = currentStoringLocation.getmId();
                LatLng position = currentStoringLocation.getmLatLng();
                int radius = currentStoringLocation.getRadius();

                GeofenceHelper.startGeofence(getApplicationContext(), location_id, position, radius);

                onMapReady(mMap);
                drawGeofence(currentGeofenceMarker, currentStoringLocation.getRadius());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}