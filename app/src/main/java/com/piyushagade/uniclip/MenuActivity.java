package com.piyushagade.uniclip;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    private SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private Handler handler_devices, handler_history;
    private ArrayList<String> history_list_activity;
    private ClipboardManager myClipboard;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_menu);

        //Get SharedPreferences
        sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();

        //Get Intent
        intent = getIntent();

        //Setup displays
        if(sp.getString("user_email", "unknown")!= "unknown" || sp.getString("user_email", "unknown")!= "deleted_user")
            ((TextView)findViewById(R.id.menu_user_username)).setText(sp.getString("user_email", "Error retrieving info."));
        if(sp.getInt("access_pin", 0)!= 0)
            ((TextView)findViewById(R.id.menu_user_access_pin)).setText(String.valueOf(sp.getInt("access_pin", 0)));
        if(sp.getString("device_name", "unknown")!= "unknown")
            ((TextView)findViewById(R.id.menu_user_device)).setText(sp.getString("device_name", "Error retrieving info."));

        if(intent.getStringExtra("prev_activity") != null && intent.getStringExtra("prev_activity").equals("running_activity")){
            findViewById(R.id.rl_history).setVisibility(View.VISIBLE);
            findViewById(R.id.rl_first_page).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.menu_title)).setText("History.");


            handler_history  = new Handler();
            handler_history.postDelayed(refreshHistory, 0);
        }

        if(intent.getStringExtra("prev_activity") != null && intent.getStringExtra("prev_activity").equals("main_activity")){
            findViewById(R.id.menu_history).setVisibility(View.GONE);
            findViewById(R.id.menu_user_info).setVisibility(View.GONE);
        }

        //Back (Close menu) button listener
        findViewById(R.id.menu_back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                if(((TextView)findViewById(R.id.menu_title)).getText().toString().equals("Menu.")){
                    if(handler_devices != null) handler_devices.removeCallbacks(refreshDevicesList);
                    if(handler_history != null) handler_history.removeCallbacks(refreshHistory);
                    finish();
                }
                else if(intent.getStringExtra("prev_activity") != null && intent.getStringExtra("prev_activity").equals("running_activity")){
                    if(handler_devices != null) handler_devices.removeCallbacks(refreshDevicesList);
                    if(handler_history != null) handler_history.removeCallbacks(refreshHistory);
                    finish();
                }
                else{
                    findViewById(R.id.rl_history).setVisibility(View.GONE);
                    findViewById(R.id.rl_info).setVisibility(View.GONE);
                    findViewById(R.id.rl_help).setVisibility(View.GONE);
                    findViewById(R.id.rl_user).setVisibility(View.GONE);

                    ((TextView)findViewById(R.id.menu_title)).setText("Menu.");
                    findViewById(R.id.rl_first_page).setVisibility(View.VISIBLE);
                }
            }
        });

        //History button listener
        findViewById(R.id.menu_history).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                findViewById(R.id.rl_history).setVisibility(View.VISIBLE);
                findViewById(R.id.rl_first_page).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.menu_title)).setText("History.");


                handler_history  = new Handler();
                handler_history.postDelayed(refreshHistory, 0);
            }
        });



        //User button listener
        findViewById(R.id.menu_user_info).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                (findViewById(R.id.rl_user)).setVisibility(View.VISIBLE);
                findViewById(R.id.rl_first_page).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.menu_title)).setText("User info.");

                int key = sp.getInt("access_pin", 0);

                handler_devices  = new Handler();
                handler_devices.postDelayed(refreshDevicesList, 0);
            }
        });

        //Developer Info button listener
        findViewById(R.id.menu_developer).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                (findViewById(R.id.rl_info)).setVisibility(View.VISIBLE);
                findViewById(R.id.rl_first_page).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.menu_title)).setText("Developer.");

            }
        });


        //Help button listener
        findViewById(R.id.menu_help).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                startActivity(new Intent(MenuActivity.this, TutorialActivity.class));

            }
        });

        //Clear history listener
        findViewById(R.id.b_clear_history).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                history_list_activity.clear();

                if(UniClipService.history_list_service != null) UniClipService.history_list_service.clear();
                setHistoryListItems();
            }
        });

        //Download desktop client button listener
        findViewById(R.id.b_menu_developer_desktop_client).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                String url = "http://piyushagade.xyz/uniclip";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        //Donate button listener
        findViewById(R.id.b_menu_developer_donate).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                String url = "https://www.paypal.me/piyushagade";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        //Email developer button listener
        findViewById(R.id.b_menu_developer_email).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                String url = "mailto:piyushagade@gmail.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }



    //Set Clipboard History Lists
    private void syncHistoryLists() {
        if(isServiceRunning(UniClipService.class)){
            history_list_activity = UniClipService.history_list_service;
        }
    }

    //Check if service is running
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
        View v = findViewById(R.id.rl_main);
        Snackbar.make(v, t, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    //Make Snack for Permission
    public void makeSnackForPermissions(String t){
        View v = findViewById(R.id.rl_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
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

    //Vibrate method
    private void vibrate(int time){
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
    }

    //Make Toast
    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
    }

    //Get System Device Name
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        }
        return manufacturer + " " + model;
    }


    //History refresh with 4 sec delay
    private Runnable refreshHistory = new Runnable() {
        public void run() {
            Log.d("Runnables", "History refreshed.");
            setHistoryListItems();

            handler_history.postDelayed(this, 4000);
        }
    };


    //Refresh device list runnable with 4 sec delay
    private Runnable refreshDevicesList = new Runnable() {
        public void run() {
            Log.d("Runnables", "Devices list refreshed.");
            setRegisteredDeviceList();

            handler_devices.postDelayed(this, 4000);
        }
    };


    //Set devices in feed
    private void setRegisteredDeviceList() {
        int i = 1;
        final LinearLayout ll_other_devices_feed = (LinearLayout) findViewById(R.id.ll_other_devices_feed);
        ll_other_devices_feed.removeAllViews();

        DatabaseReference fb_devices = mRootRef.child("cloudboard").child(encrypt(encrypt(encrypt(sp.getString("user_email", "unknown").replaceAll("\\.", "")))) + "/devices");

        fb_devices.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ll_other_devices_feed.removeAllViews();

                int i = 1;    //Serial number
                if (snapshot.getChildrenCount() != 0)
                    for (final DataSnapshot postSnapshot : snapshot.getChildren()) {

                        if(postSnapshot.getValue().toString().equals("0") ||
                                postSnapshot.getValue().toString().equals("1")) {
                            final TextView row1 = new TextView(getBaseContext());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                row1.generateViewId();
                            }
                            row1.setPadding(20, 12, 20, 12);

                            row1.setText(i + ". " + postSnapshot.getKey().toString());
                            i++;

                            if (postSnapshot.getValue().toString().equals("1"))
                                row1.setTextColor(Color.parseColor("#AA000000"));
                            else if (postSnapshot.getValue().toString().equals("0"))
                                row1.setTextColor(Color.parseColor("#33000000"));
                            row1.setTextSize(16);

                            row1.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    if (postSnapshot.getValue().toString().equals("1"))
                                        makeToast(postSnapshot.getKey().toString() + " is listening.");
                                    else if (postSnapshot.getValue().toString().equals("0"))
                                        makeToast(postSnapshot.getKey().toString() + " is inactive.");
                                }

                            });

                            ll_other_devices_feed.addView(row1);
                            ll_other_devices_feed.setVisibility(View.VISIBLE);
                        }
                        else{
                            //2 - windows
                            //3 - Mac
                            //4  -Linux
                            final TextView row1 = new TextView(getBaseContext());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                row1.generateViewId();
                            }
                            row1.setPadding(20, 12, 20, 12);
                            row1.setTextColor(Color.parseColor("#AA000000"));

                            String variant_num = postSnapshot.getValue().toString().split("%")[0];
                            String[] variant = {"Windows", "Linux", "Mac"};

                            row1.setText(i + ". " + variant[Integer.valueOf(variant_num) - 2] + " desktop");
                            i++;

                            row1.setTextSize(16);

                            row1.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    makeToast("Desktop" + " is listening.");
                                }

                            });

                            ll_other_devices_feed.addView(row1);
                            ll_other_devices_feed.setVisibility(View.VISIBLE);
                        }
                    }
                else {
                    final TextView row1 = new TextView(getBaseContext());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        row1.generateViewId();
                    }
                    row1.setPadding(20, 12, 20, 12);
                    row1.setText("No registered devices.");

                    row1.setTextColor(Color.parseColor("#AA000000"));
                    row1.setTextSize(16);

                    ll_other_devices_feed.addView(row1);
                    ll_other_devices_feed.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

    }


    //Set history feed
    private void setHistoryListItems() {
        syncHistoryLists();

        int i = 1;
        LinearLayout ll_history_feed = (LinearLayout) findViewById(R.id.ll_history_feed);
        ll_history_feed.removeAllViews();

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if(history_list_activity != null)
            if(history_list_activity.size() != 0)
                for (final String listItem : history_list_activity) {
                    final TextView row1 = new TextView(getBaseContext());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        row1.generateViewId();
                    }
                    row1.setPadding(20, 12, 30, 12);

                    row1.setText(i + ". " + listItem.toString());
                    row1.setMaxLines(7);
                    i++;

                    if(isEven(i))row1.setTextColor(Color.parseColor("#CC000000"));
                    else row1.setTextColor(Color.parseColor("#88000000"));

                    row1.setTextSize(16);

                    row1.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            ClipData myClip = ClipData.newPlainText("text", String.valueOf(listItem));
                            makeToast("Text copied to clipboard.");
                            myClipboard.setPrimaryClip(myClip);
                        }

                    });

                    RelativeLayout row2 = new RelativeLayout(getBaseContext());
                    row2.setBackgroundColor(Color.parseColor("#AA009688"));
                    row2.setMinimumHeight(2);


                    RelativeLayout row3 = new RelativeLayout(getBaseContext());
                    row3.setBackgroundColor(Color.parseColor("#00009688"));
                    row3.setMinimumHeight(15);

                    RelativeLayout row4 = new RelativeLayout(getBaseContext());
                    row4.setBackgroundColor(Color.parseColor("#00009688"));
                    row4.setMinimumHeight(15);

                    ll_history_feed.addView(row1);
                    ll_history_feed.addView(row3);
                    ll_history_feed.addView(row2);
                    ll_history_feed.addView(row4);
                    ll_history_feed.setVisibility(View.VISIBLE);
                }
            else{
                final TextView row1 = new TextView(getBaseContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    row1.generateViewId();
                }
                row1.setPadding(20, 12, 20, 12);
                row1.setText("Clipboard history is empty!");

                row1.setTextColor(Color.parseColor("#88000000"));
                row1.setTextSize(16);

                ll_history_feed.addView(row1);
                ll_history_feed.setVisibility(View.VISIBLE);

            }
    }

    @Override
    protected void onPause() {
        //Remove callbacks
        if(handler_devices != null) handler_devices.removeCallbacks(refreshDevicesList);
        if(handler_history != null) handler_history.removeCallbacks(refreshHistory);

        finish();
        super.onPause();
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


    //Check if a number is even
    private boolean isEven(int i) {
        if(i % 2 == 0) return true;
        return false;
    }
}
