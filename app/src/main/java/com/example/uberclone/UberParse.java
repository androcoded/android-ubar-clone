package com.example.uberclone;

import android.app.Application;

import com.parse.Parse;

public class UberParse extends Application {
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("xolJmSMjgwQ4khvkmyVwr0ijdavYZJPinMKd9KMk")
                .clientKey("X74i2HjocCikw4yufSaKy5ptNVKZ2HQ5XSFw3Izs")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
