package com.piyushagade.uniclip;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class ManageFriendsActivity extends Activity{

    private ImageView iv_logo, iv_slogan, b_menu;
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private static final String FREINDS_FILE = "com.piyushagade.uniclip.friends";
    boolean sp_first_run;
    private SharedPreferences.Editor ed;
    private String prev_screen;
    private Intent intent;
    ImageView b_close;
    private ArrayList<String> friends_list;
    private ArrayList<String> selected_list;
    private Button b_friends_manage_add, b_friends_manage_delete;
    private EditText et_add_friend_email;
    private LinearLayout ll_get_friends_feed, ll_add_friend;
    private Firebase fb;
    private boolean userDoesntExists;
    private TextView friends_feed_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_manage);
        Firebase.setAndroidContext(this);

        //Get SharedPreferences
        SharedPreferences sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();


        //Firebase
        fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/");

        //Get intent data
        intent = getIntent();
        prev_screen = intent.getStringExtra("prev_screen");

        //UI Components
        iv_logo = (ImageView) findViewById(R.id.app_logo);
        b_close = (ImageView) findViewById(R.id.b_close);
        b_friends_manage_add = (Button) findViewById(R.id.b_friends_manage_add);
        b_friends_manage_delete = (Button) findViewById(R.id.b_friends_manage_delete);
        et_add_friend_email = (EditText) findViewById(R.id.et_add_friend_email);
        b_menu = (ImageView) findViewById(R.id.b_menu);
        ll_get_friends_feed = (LinearLayout) findViewById(R.id.ll_get_friends_feed);
        ll_add_friend = (LinearLayout) findViewById(R.id.ll_add_friend);
        friends_feed_title = (TextView) findViewById(R.id.friends_feed_title);


        //Lists:
        //Send List
        selected_list = new ArrayList<String>();

        //Friends list
        friends_list = new ArrayList<String>();

        //Set feed & friends_list array
        setFriendsList();

        //Make menu button disappear
        b_menu.setVisibility(View.GONE);

        //Close Button listener
        b_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(prev_screen.equals("cloud_share")) {
                    startActivity(new Intent(ManageFriendsActivity.this, GetFriendNodeActivity.class));
                    finish();
                }
                else if(prev_screen.equals("main_screen_menu"))
                    finish();
            }

        });

        //Add Friend/ Add Button listener
        b_friends_manage_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (b_friends_manage_add.getText().equals("Add a friend")) {
                    ll_get_friends_feed.setVisibility(View.GONE);
                    ll_add_friend.setVisibility(View.VISIBLE);
                    b_friends_manage_add.setText("Add");
                    friends_feed_title.setText("Add friend:");
                    b_friends_manage_delete.setText("Go back");

                    vibrate(50);
                }
                else if (b_friends_manage_add.getText().equals("Add")) {
                    userDoesntExists = true;

                    fb.child((et_add_friend_email.getText().toString().replaceAll("\\.", "")))
                            .child("key").addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                if (!snapshot.getValue().toString().equals(""))
                                {   //User has account in UniClip
                                    userDoesntExists = false;
                                    loadArray();
                                    if(!et_add_friend_email.getText().toString().equals(""))
                                        if(!friends_list.contains(et_add_friend_email.getText().toString()))
                                        {
                                            friends_list.add(et_add_friend_email.getText().toString());
                                            makeToast("Added to your list.");
                                        }
                                        else {
                                            makeToast("Already in your list.");
                                        }

                                    saveArray();
                                }

                            }

                            else {
                                //User has no account in UniClip
                                userDoesntExists = true;
                                if(!et_add_friend_email.getText().toString().equals(""))
                                    makeToast("User not in our database.");
                                else if(et_add_friend_email.getText().toString().equals("")){
                                    makeToast("Enter email of your friend.");
                                }
                                swingAnimate(et_add_friend_email, 700, 200);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError error) {
                        }
                    });



                    vibrate(50);
                }
            }

        });

        //Delete Friend/ Go back Button listener
        b_friends_manage_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (b_friends_manage_delete.getText().equals("Delete selected")) {
                    loadArray();
                    if (selected_list.size() != 0){
                        for (String friend : selected_list) {
                            friends_list.remove(friend);
                        }

                        saveArray();

                        setFriendsList();
                        vibrate(50);
                    }
                    else
                        makeToast("No friends selected.");
                }
                else if (b_friends_manage_delete.getText().equals("Go back")) {
                    setFriendsList();

                    ll_get_friends_feed.setVisibility(View.VISIBLE);
                    ll_add_friend.setVisibility(View.GONE);
                    b_friends_manage_delete.setText("Delete selected");
                    b_friends_manage_add.setText("Add a friend");

                    friends_feed_title.setText("Friends list:");

                    vibrate(50);
                }
            }

        });



    }


//    friends_list.add("rohan2005p@gmail.com");
//    friends_list.add("ninadmundalik@gmail.com");
//    friends_list.add("stunningguy786@gmail.com");
//    friends_list.add("arnavbhartiya@gmail.com");

    private String setFriendsList() {
        loadArray();

        //Set feed
        final LinearLayout ll_friends_feed = (LinearLayout) findViewById(R.id.ll_get_friends_feed);
        ll_friends_feed.removeAllViews();

        int i = 1;

        if(friends_list.size() != 0) {
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

                        if (!selected_list.contains(listItem.toString())) {
                            //Select
                            selected_list.add(listItem.toString());
                            row1.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        } else {
                            //Deselect
                            selected_list.remove(listItem.toString());
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
            row1.setText("You have 0 friends in your list.");

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


    public void makeSnack(String t){
        View v = findViewById(R.id.rl_main);
        Snackbar.make(v, t, Snackbar.LENGTH_LONG).setAction("Action", null).show();
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

    //Swing animate view
    private void swingAnimate(final View v, final int duration, final int delay){
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

}
