package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    //Static constant fields
    private static final String TAG = "PassengerActivity";
    private static final int LOCATION_PERMISSION = 1000;

    //vars
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Boolean isUbarCancelled = true;

    //widgets
    private Button btnRequestCar;
    private Button btnLogoutPassengerActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnRequestCar = findViewById(R.id.btnRequestCar);
        btnRequestCar.setOnClickListener(this);
        checkingCurrentUbarRequest();
        btnLogoutPassengerActivity = findViewById(R.id.btnLogoutFromPassengerActivity);
        btnLogoutPassengerActivity.setOnClickListener(this);
    }

    private void checkingCurrentUbarRequest() {
        ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
        requestCarQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size()>0 && e == null){
                    btnRequestCar.setText("Cancel your ubar request");
                    isUbarCancelled = false;
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: updating passenger location");
                updateCameraPassengerLocation(location);
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
        };
        if (Build.VERSION.SDK_INT < 23) {
            Log.d(TAG, "onMapReady: permission is granted hence sdk below 23");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            Location currentPassengerLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateCameraPassengerLocation(currentPassengerLocation);
        } else if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PassengerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
            } else {
                Log.d(TAG, "onMapReady: permission is granted!");
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Location currentPassengerLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }

    }

    private void updateCameraPassengerLocation(Location location) {

        LatLng passengerLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(passengerLocation));
        mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here!"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Location currentPassengerLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    //Button for requesting a car
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRequestCar:
        if (isUbarCancelled){
        Log.d(TAG, "onClick: Requesting a car if the location is available");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            Location currentPassengerLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (currentPassengerLocation!=null){
                    ParseObject requestCar = new ParseObject("RequestCar");
                    requestCar.put("username",ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint userLocation = new ParseGeoPoint(currentPassengerLocation.getLatitude(),currentPassengerLocation.getLongitude());
                    requestCar.put("userLocation",userLocation);
                    requestCar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                Log.d(TAG, "done: Saving the location in RequestCar class");
                                Toast.makeText(PassengerActivity.this, "Successfully sent car request", Toast.LENGTH_SHORT).show();
                                btnRequestCar.setText("Cancel your ubar request");
                                isUbarCancelled = false;
                            }
                        }
                    });
                }else{
                    Toast.makeText(this, "Something wrong, location does not found!", Toast.LENGTH_SHORT).show();
                }
        }
        }else {
            ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size()>0 && e == null){
                        for (ParseObject i : objects){
                            i.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null){
                                        Log.d(TAG, "done: Deleting parse request");
                                        Toast.makeText(PassengerActivity.this, "Successfully cancelled your ubar request", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        isUbarCancelled = true;
                        btnRequestCar.setText("Request Car");
                    }
                }
            });
        }
            break;
            case R.id.btnLogoutFromPassengerActivity:
                if (ParseUser.getCurrentUser()!=null){
                    ParseUser.logOut();
                    finish();
                    startActivity(new Intent(PassengerActivity.this,SignUpActivity.class));
                }
                break;
        }
    }
}


