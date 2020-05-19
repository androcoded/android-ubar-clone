package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    private String userType="";
    private ProgressDialog progressDialog;
    private Button btnSignUp;
    private EditText edtUsername,edtPassword;
    private enum State{
        SIGNUP,SIGNIN;
    }
    private State state;
    private RadioButton rdbPassenger,rdbDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar tlbSignUp = findViewById(R.id.tlbSignUp);
        edtUsername = findViewById(R.id.edtSignUpUsername);
        edtPassword = findViewById(R.id.edtSignUpPassword);
        setSupportActionBar(tlbSignUp);
        if (ParseUser.getCurrentUser()!=null){
            transitionToPassengerActivity();
            transitionToDriverActivity();
        }
        state = State.SIGNUP;
        rdbPassenger = findViewById(R.id.rdbPassenger);
        rdbDriver = findViewById(R.id.rdbDriver);
        btnSignUp = findViewById(R.id.btnSignUP);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == State.SIGNUP){
                    signUpParseUser();
                }else if (state == State.SIGNIN){
                    logInParseUser();
                }
            }
        });
        Button btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);
        btnOneTimeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                annonymousUserParse();    
            }
        });
    }

    private void annonymousUserParse() {
        final EditText edtDriverOrPassenger = findViewById(R.id.edtDriverOrPassenger);
        if(edtDriverOrPassenger.getText().toString().equals("Driver")||edtDriverOrPassenger.getText().toString().equals("Passenger")){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (user!=null&&e==null){
                        Toast.makeText(SignUpActivity.this, "Logged in as a guest user", Toast.LENGTH_SHORT).show();
                        user.put("userType",edtDriverOrPassenger.getText().toString());
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                               transitionToPassengerActivity();
                               transitionToDriverActivity();
                            }
                        });
                    }
                }
            });
        }else{
            Toast.makeText(this, "Please put a Driver/Passenger", Toast.LENGTH_SHORT).show();
        }
    }

    private void signUpParseUser(){
        try {

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Signing Up...");
            progressDialog.show();
            ParseUser signUpUser = new ParseUser();
            signUpUser.setUsername(edtUsername.getText().toString());
            signUpUser.setPassword(edtPassword.getText().toString());
            if (!rdbPassenger.isChecked()&&!rdbDriver.isChecked()){
                progressDialog.dismiss();
                Toast.makeText(this, "You must select Passenger/Driver", Toast.LENGTH_SHORT).show();
            }else{
                if (rdbPassenger.isChecked()){
                    userType = "Passenger";
                }else if (rdbDriver.isChecked()){
                    userType = "Driver";
                }
                signUpUser.put("userType",userType);
                signUpUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(SignUpActivity.this, edtUsername.getText().toString()
                                    + " is signed up successfully!", Toast.LENGTH_SHORT).show();
                            transitionToDriverActivity();
                            transitionToPassengerActivity();
                        } else {
                            Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logInParseUser(){
        ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null && e == null){
                    Toast.makeText(SignUpActivity.this, user.getUsername()+" is successfully logged in!", Toast.LENGTH_SHORT).show();
                    transitionToPassengerActivity();
                    transitionToDriverActivity();
                }else{
                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void transitionToPassengerActivity(){
        if (ParseUser.getCurrentUser().get("userType").equals("Passenger")){
            startActivity(new Intent(this,PassengerActivity.class));
            finish();
        }
    }

    private void transitionToDriverActivity(){
        if (ParseUser.getCurrentUser().get("userType").equals("Driver")){
            startActivity(new Intent(this,DriverRequestListActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.mniLogin:
                if (state == State.SIGNUP){
                    state = State.SIGNIN;
                    item.setTitle("Sign Up");
                    btnSignUp.setText("Log In");
                }else if (state == State.SIGNIN){
                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUp.setText("Sign Up");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
