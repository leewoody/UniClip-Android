package com.piyushagade.uniclip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.firebase.client.Firebase;

public class LoginActivity extends Activity{

    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private TextView myTextView;
    private QRCodeReaderView mydecoderview;

    private Firebase fb, fb_desktops;
    private String sp_user_email;
    SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private String user_node, user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.login_layout);

        Firebase.setAndroidContext(this);

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
        //Close Button listener
        (findViewById(R.id.register)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ed.putString("sp_user_email", ((EditText) findViewById(R.id.login_email)).getText().toString()).commit();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
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

}