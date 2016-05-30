package com.piyushagade.uniclip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.LinkedList;

public class GetFriendNodeActivity extends Activity{

    private ImageView iv_logo, iv_slogan, b_menu;
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private static final String FREINDS_FILE = "com.piyushagade.uniclip.friends";
    private SharedPreferences.Editor ed;
    private String data;
    private Intent intent;
    private Button b_share, b_manage_friends;
    ImageView b_close;
    private ArrayList<String> friends_list;
    private ArrayList<String> send_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_get_friends);
        Firebase.setAndroidContext(this);

        //Get SharedPreferences
        SharedPreferences sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();

        //Get data to be shared
        intent = getIntent();
        data = intent.getStringExtra("data_shared");


        //Firebase
        final Firebase fb = new Firebase("https://uniclipold.firebaseio.com/cloudboard/");

        //UI Components
        iv_logo = (ImageView) findViewById(R.id.app_logo);
        b_manage_friends = (Button) findViewById(R.id.b_manage_friends);
        b_close = (ImageView) findViewById(R.id.b_close);
        b_share = (Button) findViewById(R.id.b_share);
        b_menu = (ImageView) findViewById(R.id.b_menu);

        //Lists:
        //Send List
        send_list = new ArrayList<String>();

        //Friends list
        friends_list = new ArrayList<String>();

        //Get friend node name
        String friend_node = getFriendNode();

        //Make menu buton disappear
        b_menu.setVisibility(View.GONE);

        //Close Button listener
        b_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }

        });

        //Share Button listener
        b_share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(send_list.size() != 0)
                    for(String friend: send_list){
                        friend = encrypt(encrypt(encrypt(friend.replaceAll("\\.", ""))));

                        //Set data on firebase
                        fb.child(friend).child("data").setValue(data);
                        finish();
                    }
                else
                    makeToast("No friends selected.");

            }

        });

        //Manage Friends Button listener
        b_manage_friends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(GetFriendNodeActivity.this, ManageFriendsActivity.class).putExtra("prev_screen", "cloud_share"));
                finish();
            }

        });


    }

    private String getFriendNode() {
        loadArray();

        //Set feed
        final LinearLayout ll_friends_feed = (LinearLayout) findViewById(R.id.ll_get_friends_feed);
        ll_friends_feed.removeAllViews();


        int i = 1;
        if(friends_list.size() !=0){
            for (final String listItem : friends_list) {
                final TextView row1 = new TextView(getBaseContext());
                row1.generateViewId();
                row1.setPadding(20, 12, 30, 12);

                row1.setText(" " + i + ".  " + listItem.toString());
                i++;

                row1.setTextColor(Color.parseColor("#CCFFFFFF"));

                row1.setTextSize(16);
                row1.setPadding(16, 12, 16, 10);
                row1.setHeight(100);
                row1.setBackgroundColor(Color.parseColor("#00FFFFFF"));

                row1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        if (!send_list.contains(listItem.toString())) {
                            //Select
                            send_list.add(listItem.toString());
                            row1.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        } else {
                            send_list.remove(listItem.toString());
                            row1.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                        }
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

                ll_friends_feed.addView(row1);
                ll_friends_feed.addView(row3);
                ll_friends_feed.addView(row2);
                ll_friends_feed.addView(row4);
                ll_friends_feed.setVisibility(View.VISIBLE);
            }
        }
        else{
            final TextView row1 = new TextView(getBaseContext());
            row1.generateViewId();
            row1.setPadding(20, 12, 20, 12);
            row1.setText("You have 0 friends in your list. \n\nUse 'Manage friends' button to add some.");

            row1.setTextColor(MainActivity.colorPrimary);
            row1.setTextSize(16);

            ll_friends_feed.addView(row1);
            ll_friends_feed.setVisibility(View.VISIBLE);
        }

        return null;
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

    private void vibrate(int time){
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
    }

    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(), String.valueOf(data), Toast.LENGTH_LONG).show();
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

    //Persist Friends
    void saveArray()
    {
        SharedPreferences sp = getSharedPreferences(FREINDS_FILE, 0);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("friends_list_size", friends_list.size());

        int i = 0;
        for(String item: friends_list)
        {
            ed.putString(String.valueOf(i), friends_list.get(i));
            i++;
        }
        ed.commit();
    }

    void loadArray()
    {
        SharedPreferences sp = getSharedPreferences(FREINDS_FILE, 0);
        friends_list.clear();
        int size = sp.getInt("friends_list_size", 0);

        for(int  i= 0; i < size; i++)
        {
            if(friends_list != null) friends_list.add(sp.getString(String.valueOf(i), "No_one"));
        }
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

}
