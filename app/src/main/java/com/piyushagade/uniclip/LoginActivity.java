package com.piyushagade.uniclip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import static android.view.View.GONE;
import static com.piyushagade.uniclip.R.anim.rotate;

public class LoginActivity extends Activity{

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private TextView myTextView;
    private QRCodeReaderView mydecoderview;

    private DatabaseReference fb, fb_desktops;
    private String sp_user_email;
    SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private String user_node, user_email;
    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.layout_login);

        //Read SharedPreferences
        sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();
        sp_user_email = sp.getString("user_email", "unknown");

        if(hasPermission()) {
            AccountManager accountManager = AccountManager.get(LoginActivity.this);
            Account account = getAccount(accountManager);

            if (account != null) {
                ((EditText) findViewById(R.id.login_email)).setText(account.name);
            }
        }


        //OnCLick Listeners

        //Verify email button listener
        findViewById(R.id.b_verify_email).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);


                Log.d(TAG, "Verify email  button pressed.");

                if(!isNetworkAvailable()){
                    makeSnack("Network unavailable");
                }

                final String input_pin = ((EditText) findViewById(R.id.verify_email_code)).getText().toString();

                int correct_pin = sp.getInt("login_verification_code", 0);

                //If correct verification code input
                if (input_pin.equals(String.valueOf(correct_pin))){

                    ed.putString("user_email", sp.getString("login_new_email", "unknown")).commit();
                    ed.putString("login_new_email", "none").commit();
                    ed.putBoolean("authenticated", true).commit();

                    //Log activity
                    Log.d(TAG, "Verified. Service will be started");

                    //Start Service
                    Intent intent = new Intent(LoginActivity.this, UniClipService.class);
                    intent.putExtra("isAutorun", "false");
                    startService(intent);

                    finish();
                }
                else {
                    makeToast("Wrong verification code. Try again.");
                    vibrate(50);


                    //Log activity
                    Log.d(TAG, "Wrong verification code.");

                    //Animate input on wrong password
                    swingAnimate(findViewById(R.id.verify_email_code), 600, 300);

                    ed.putBoolean("authenticated", false).commit();

                }

            }
        });


        //Login Button listener
        (findViewById(R.id.b_login)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                sp_user_email = ((TextView)findViewById(R.id.login_email)).getText().toString();

                //Format email address (remove the .)
                String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));

                //Firebase
                DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

                //Check if user node exists
                fb.child("key").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        //If user account doesn't exists
                        if (!snapshot.exists()) {
                            String new_email = sp_user_email;
                            findViewById(R.id.rl_login_section).setVisibility(View.GONE);
                            findViewById(R.id.rl_login_email_verification).setVisibility(View.VISIBLE);
                            ((TextView)findViewById(R.id.login_title)).setText("Verify your email");

                            ed.putString("login_new_email", new_email).commit();


                            //Send confirmation email with PIN
                            String to = new_email;
                            String subject = "UniClip! account verification.";
                            int verification_code  = new Random().nextInt(99999);
                            ed.putInt("login_verification_code", verification_code).commit();

                            String body = "Your UniClip verification code: " + verification_code;


                            //Creating SendMail object
                            Mail sm = new Mail(LoginActivity.this, to, subject , body);
                            try{
                                sm.execute();
                            }catch (Exception e){
                                // Do nothing
                            }

                        }

                        //If user account exists
                        else{
                            ed.putString("user_email", sp_user_email).commit();
                            ed.putBoolean("authenticated", true).commit();

                            //Start service
                            Intent intent = new Intent(LoginActivity.this, UniClipService.class);
                            intent.putExtra("isAutorun", "false");
                            startService(intent);

                            //Log activity
                            Log.d(TAG, "Service started");

                            startActivity(new Intent(LoginActivity.this, RunningActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });

            }

        });

        // Verify new email
        findViewById(R.id.b_verify_email_resend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                Log.d(TAG, "Resend button pressed.");

                String to = sp.getString("login_new_email", "");
                String subject = "UniClip! account verification";
                int verification_code  = new Random().nextInt(99999);
                ed.putInt("login_verification_code", verification_code).commit();

                String body = "Your UniClip! verification code: " + verification_code;


                //Creating SendMail object
                Mail sm = new Mail(LoginActivity.this, to, subject , body);
                sm.execute();

                findViewById(R.id.b_verify_email_resend).setVisibility(View.INVISIBLE);

            }
        });

    }


    //Vibrate method
    private void vibrate(int time){
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
    }

    //Make Toast
    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
    }

    //Check if Network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Make Snack
    public void makeSnack(String t){
        View v = findViewById(R.id.rl_decoder);
        Snackbar.make(v, t, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private boolean hasPermission()
    {
        String permission = "android.permission.GET_ACCOUNTS";
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }

    //Encrypt function
    private String encrypt(String data) {
        int k = data.length();
        int m = (k + 1)/2;

        char raw[] = data.toCharArray();
        char temp[] = new char[k];

        for(int j = 0; j < k; j++){
            if(j >= 0 && j < m){
                temp[2*j] = raw[j];
            }
            else if(j >= m  && j <= k - 1){
                if(k % 2 == 0) temp[2*j - k + 1] = raw[j];
                else temp[2*j - k] = raw[j];
            }
        }

        return String.valueOf(temp);
    }

    //Decrypt function
    private String decrypt(String data){
        int k = data.length();
        int m = (k + 1)/2;

        char raw[] = data.toCharArray();
        char temp[] = new char[k];

        for(int j = 0; j < k; j++){
            if(j >= 0 && j < m){
                temp[j] = raw[2*j];
            }
            else if(j >= m  && j <= k - 1){
                if(k % 2 == 0) temp[j] = raw[2*j - k + 1];
                else temp[j] = raw[2*j - k];
            }

        }
        return String.valueOf(temp);
    }

    //Swing animate view
    private void swingAnimate(final View v, final int duration, final int delay) {
        //App title animation
        Handler app_title_anim_handler = new Handler();
        app_title_anim_handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                YoYo.with(Techniques.Swing)
                        .duration(duration)
                        .playOn(v);
            }
        }, delay);
    }

}