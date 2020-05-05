package com.example.uberclone;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener mLocationListener;
    private Button btnRequestCar;
    private boolean isUberCancelled = true;

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

        ParseQuery<ParseObject> query = ParseQuery.getQuery("RequestCar");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size()>0 && e == null){

                    btnRequestCar.setText("Cancel uber car!!");
                    isUberCancelled = false;
                }
            }
        });



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
    }


    // Checking location permission using third party library TedPermission
    private void checkLocationPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onPermissionGranted() {
                Toast.makeText(PassengerActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                mLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        updatingCurrentPassengerLocation(location);

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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,mLocationListener);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(PassengerActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .check();

    }

    //Updating the location of current passenger
    private void updatingCurrentPassengerLocation(Location pLocation){
        LatLng latLng = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.addMarker(new MarkerOptions().position(latLng).title("You are here!!"));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View v) {
        if (isUberCancelled){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,mLocationListener);
            Location passengerLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (passengerLastLocation!=null){

                ParseObject requestCar = new ParseObject("RequestCar");
                requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                ParseGeoPoint userLocation = new ParseGeoPoint(passengerLastLocation.getLatitude(),passengerLastLocation.getLongitude());
                requestCar.put("passengerLocation",userLocation);
                requestCar.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null){
                            Toast.makeText(PassengerActivity.this, "Sent a request", Toast.LENGTH_SHORT).show();
                            btnRequestCar.setText("Cancel uber car!!");
                            isUberCancelled = false;
                        }else{
                            Toast.makeText(PassengerActivity.this, e.getMessage()+"", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }else{
                Toast.makeText(this, "Unknown error!", Toast.LENGTH_SHORT).show();
            }
        }else{
            ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size()>0 && e == null){
                            for (ParseObject uberRequest: objects){
                                uberRequest.deleteInBackground();
                            }
                    }else{
                        Toast.makeText(PassengerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    btnRequestCar.setText("Request car");
                    isUberCancelled = true;
                }
            });
        }


    }
}