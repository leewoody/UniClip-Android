package com.piyushagade.uniclip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class SplashActivity extends Activity{

    private ImageView app_logo, app_slogan;
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    boolean sp_first_run;
    private TextView app_version;
    private RelativeLayout rl_splash_bottom;
    private int delayTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        //Get SharedPreferences
        SharedPreferences sp = getSharedPreferences(PREF_FILE, 0);

        //UI Components
        app_logo = (ImageView) findViewById(R.id.app_logo);
        app_version = (TextView) findViewById(R.id.app_version);
        rl_splash_bottom = (RelativeLayout) findViewById(R.id.rl_splash_bottom);

        //Variable splash screen display time
        if(isServiceRunning(UniClipService.class))
            delayTime = 1000;
        else
            delayTime = 2800;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, delayTime);
    }


    //fadeAnimate view
    private void fadeAnimate(final View v, final int duration, final int delay){
        //App title animation
        Handler app_title_anim_handler = new Handler();
        app_title_anim_handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                YoYo.with(Techniques.FadeInUp)
                        .duration(duration)
                        .playOn(v);
            }
        }, delay);
    }

    //slideInUpAnimate view
    private void slideInUpAnimate(final View v, final int duration, final int delay){
        //App title animation
        Handler app_title_anim_handler = new Handler();
        app_title_anim_handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                YoYo.with(Techniques.SlideInUp)
                        .duration(duration)
                        .playOn(v);
            }
        }, delay);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void sendToast(Object charSequence){
        Toast.makeText(getBaseContext(), (String) charSequence, Toast.LENGTH_SHORT);

    }

    public void makeSnack(String t){
        View v = findViewById(R.id.rl_main);
        Snackbar.make(v, t, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void makeSnackForPermissions(String t){
        View v = findViewById(R.id.rl_main);
        Snackbar.make(v, t, Snackbar.LENGTH_LONG)
                .setAction("Let's Go", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .show();
    }

    private void vibrate(int time){
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
    }

    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
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

    private boolean hasPermission()
    {

        String permission = "android.permission.GET_ACCOUNTS";
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

}
