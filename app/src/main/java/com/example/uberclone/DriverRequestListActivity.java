package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    //Static constants
    private static final String TAG = "DriverActivity";
    private static final int LOCATION_PERMISSION = 1001;

    //widget
    private MenuItem mniDriverLogout;
    private Toolbar tlbDriverRequest;
    private Button btnRequests;
    private ListView requestsListView;

    //vars
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private ArrayList<String> driverRequests;
    private ArrayAdapter adaptor;
    private ArrayList<Double> passengerLatitudes;
    private ArrayList<Double> passengerLongitudes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);



        mniDriverLogout = findViewById(R.id.mniDriverLogout);
        tlbDriverRequest = findViewById(R.id.tlbDriverRequest);
        setSupportActionBar(tlbDriverRequest);

        btnRequests = findViewById(R.id.btnUpdateList);
        btnRequests.setOnClickListener(this);

        requestsListView = findViewById(R.id.listViewDriverRequest);
        driverRequests = new ArrayList<>();
        adaptor = new ArrayAdapter(DriverRequestListActivity.this,android.R.layout.simple_list_item_1,driverRequests);
        requestsListView.setAdapter(adaptor);
        driverRequests.clear();

        passengerLatitudes = new ArrayList<>();
        passengerLongitudes = new ArrayList<>();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DriverRequestListActivity.this,new String []{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION);

        }else {
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,mLocationListener);
                Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentLocation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.driver_logout,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.mniDriverLogout){
            ParseUser.getCurrentUser().logOut();
            startActivity(new Intent(DriverRequestListActivity.this,SignUpActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: getting location updates");
                updateRequestListView(location);
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
        if (Build.VERSION.SDK_INT<23){
            Log.d(TAG, "onClick: SDK is less than 23 hence got permission and update location");
            if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentLocation);

            }
        }else if (Build.VERSION.SDK_INT>=23){
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DriverRequestListActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION);
                Log.d(TAG, "onClick: SDK is greater than 23 hence asking permissions");
            }else{
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10,mLocationListener);
                Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==LOCATION_PERMISSION && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10,mLocationListener);
            /*Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentLocation);*/
            }
        }
    }

    private void updateRequestListView(Location driverLocation){
        if (driverLocation!=null) {
            Log.d(TAG, "updateRequestListView: driver current locations: " + driverLocation.getLongitude() + " " + driverLocation.getLongitude());
            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());
            ParseQuery<ParseObject> requestsCars = ParseQuery.getQuery("RequestCar");
            requestsCars.whereNear("userLocation", driverCurrentLocation);
            requestsCars.findInBackground(new FindCallback<ParseObject>() {
                @SuppressLint("LongLogTag")
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    try {
                        if (objects.size() > 0 && e == null) {
                            if (driverRequests.size()>0){
                                driverRequests.clear();
                            }
                            if (passengerLatitudes.size()>0){
                                passengerLatitudes.clear();
                            }
                            if (passengerLongitudes.size()>0){
                                passengerLongitudes.clear();
                            }
                            Log.d(TAG, "done: updateRequestListView: getting all nearByRequest");
                            for (ParseObject request : objects) {
                                Double distanceInKilometers = driverCurrentLocation.distanceInKilometersTo(request.getParseGeoPoint("userLocation"));
                                float roundedDistanceFromPassenger = Math.round(distanceInKilometers * 10) / 10;
                                driverRequests.add("There is a passenger away from" + roundedDistanceFromPassenger + " KMs named: " + request.get("username"));
                                passengerLatitudes.add(request.getParseGeoPoint("userLocation").getLatitude());
                                passengerLongitudes.add(request.getParseGeoPoint("userLocation").getLongitude());
                                Log.d(TAG, "done: getting all passenger location "+passengerLatitudes+"\n "+passengerLongitudes);
                            }

                        } else {
                            Toast.makeText(DriverRequestListActivity.this, "There are no request \n", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    adaptor.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
