package com.piyushagade.uniclip;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import me.leolin.shortcutbadger.ShortcutBadger;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static android.view.View.GONE;

@SuppressWarnings("unused")
public class MainActivity extends Activity {

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    private long refreshHistoryInterval = 4000;
    private long refreshConnectionStatusInterval = 6000;
    private long refreshServiceStatusInterval = 6000;
    private long refreshDevicesListInterval = 4000;

    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean runRunnables = true;
    private RelativeLayout rl_noconnection;
    private DatabaseReference fb;

    private  void stopRunnables(){
        stopRunnables = true;
        refreshHistoryInterval = 400000;
        refreshConnectionStatusInterval = 600000;
        refreshServiceStatusInterval = 600000;
        refreshDevicesListInterval = 600000;
    }

    private  void resumeRunnables(){
        stopRunnables = false;
        refreshHistoryInterval = 4000;
        refreshConnectionStatusInterval = 6000;
        refreshServiceStatusInterval = 6000;
        refreshDevicesListInterval = 4000;
    }

    @Override
    protected void onPause() {

        //Stop Runnables
        stopRunnables();

        //Stop Runnables
        if(handler_status != null) handler_status.removeCallbacks(refreshServiceStatus);
        if(handler_connection != null) handler_connection.removeCallbacks(refreshConnectionStatus);


        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //Stop Runnables
        stopRunnables();

        finish();
        super.onDestroy();
    }

    @Override
    protected void onResume() {

        //Resume Runnables
        resumeRunnables();

        super.onResume();
    }

    private Button b_start, b_clear_history, b_manage_access_pin, b_set_access_pin, b_diagnose, b_go_back_to_main, b_manage_friends, b_help_manage_friends, b_enable_overlay;
    private CheckBox cb_autostart, cb_notification, cb_vibrate, cb_theme, cb_open_url;
    private EditText input_access_pin;
    private ImageView clip_icon, sync_anim, b_close, b_menu, b_back, b_info, b_user, b_history, b_help, b_help_get, b_help_share, decode_qr;
    private SeekBar sb_get_sensitivity, sb_get_numberShakes, sb_share_sensitivity, sb_share_numberShakes;
    private TextView get_sensitivity_indicator, get_shakes_indicator, access_pin_desc, welcome_text;
    private TextView user_access_pin, status_service, status_connection;
    private TextView share_sensitivity_indicator, share_shakes_indicator;
    private RelativeLayout rl_settings, rl_running, rl_main, rl_top, rl_menu_on, rl_menu_content, rl_history, rl_info, rl_user, rl_help, rl_first_page, rl_home;

    private Animation fade_in, fade_out, rotate, blink, slide_in_top, slide_out_top, fade_in_rl_top, fade_out_rl_top, bob, fade_out_rl_home;

    private ClipboardManager myClipboard;

    private ArrayList<String> history_list_activity;

    private boolean usernode_created_now = false;

    private boolean stopRunnables;
    private Handler handler_history, handler_devices, handler_status, handler_connection;

    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private boolean sp_autostart, sp_notification, sp_vibrate, sp_theme, sp_first_run, sp_open_url, sp_are_creator, sp_authenticated;
    private String sp_user_email, sp_device_name;
    private int get_sensitivity, get_numberShakes, share_sensitivity, share_numberShakes;
    private int sp_get_sensitivity, sp_get_shakes, sp_share_sensitivity, sp_share_shakes, sp_unread;

    public static int colorPrimary, colorAccent;
    PulsatorLayout pulsator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

//        Firebase.setAndroidContext(this);


        setContentView(R.layout.activity_main);


        //UI Components
        b_start = (Button) findViewById(R.id.b_start);
        b_clear_history = (Button) findViewById(R.id.b_clear_history);
        b_enable_overlay = (Button) findViewById(R.id.b_enable_overlay);
        b_manage_access_pin = (Button) findViewById(R.id.b_manage_access_pin);
        b_set_access_pin = (Button) findViewById(R.id.b_set_access_pin);
        b_diagnose = (Button) findViewById(R.id.b_diagnose);
        b_manage_friends = (Button) findViewById(R.id.b_manage_friends);
        b_help_manage_friends = (Button) findViewById(R.id.b_help_manage_friends);
        b_go_back_to_main = (Button) findViewById(R.id.b_go_back_to_main);

        cb_autostart = (CheckBox) findViewById(R.id.cb_autostart);
        cb_notification = (CheckBox) findViewById(R.id.cb_notification);
        cb_vibrate = (CheckBox) findViewById(R.id.cb_vibrate);
        cb_theme = (CheckBox) findViewById(R.id.cb_theme);
        cb_open_url = (CheckBox) findViewById(R.id.cb_open_url);

        clip_icon = (ImageView) findViewById(R.id.clip_icon);
        sync_anim = (ImageView) findViewById(R.id.sync_anim);
        b_close = (ImageView) findViewById(R.id.b_close);
        b_menu = (ImageView) findViewById(R.id.b_menu);
        b_back = (ImageView) findViewById(R.id.b_back);
        b_user = (ImageView) findViewById(R.id.b_user);
        b_history = (ImageView) findViewById(R.id.b_history);
        b_help = (ImageView) findViewById(R.id.b_help);
        b_info = (ImageView) findViewById(R.id.b_info);
        b_help_get = (ImageView) findViewById(R.id.b_help_get);
        b_help_share = (ImageView) findViewById(R.id.b_help_share);
        decode_qr = (ImageView) findViewById(R.id.decode_qr);

        sb_get_sensitivity = (SeekBar) findViewById(R.id.sb_get_sensitivity);
        sb_get_numberShakes = (SeekBar) findViewById(R.id.sb_get_shakes);
        sb_share_sensitivity = (SeekBar) findViewById(R.id.sb_share_sensitivity);
        sb_share_numberShakes = (SeekBar) findViewById(R.id.sb_share_shakes);

        get_sensitivity_indicator = (TextView) findViewById(R.id.get_sensitivity_indicator);
        get_shakes_indicator = (TextView) findViewById(R.id.get_shakes_indicator);
        share_sensitivity_indicator = (TextView) findViewById(R.id.share_sensitivity_indicator);
        share_shakes_indicator = (TextView) findViewById(R.id.share_shakes_indicator);
        welcome_text = (TextView) findViewById(R.id.welcome_text);
        access_pin_desc = (TextView) findViewById(R.id.access_pin_desc);
        user_access_pin = (TextView) findViewById(R.id.menu_user_access_pin);
        status_connection = (TextView) findViewById(R.id.status_connection);
        status_service = (TextView) findViewById(R.id.status_service);

        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_in_rl_top = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_out_rl_top = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_out_rl_home = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        bob = AnimationUtils.loadAnimation(this, R.anim.bob);
        blink = AnimationUtils.loadAnimation(this, R.anim.blink);
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        slide_in_top = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        slide_out_top = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);

        rl_settings = (RelativeLayout) findViewById(R.id.rl_settings);
        rl_home = (RelativeLayout) findViewById(R.id.rl_home);
        rl_running = (RelativeLayout) findViewById(R.id.rl_running);
        rl_main = (RelativeLayout) findViewById(R.id.rl_main);
        rl_top = (RelativeLayout) findViewById(R.id.rl_top);
        rl_menu_on = (RelativeLayout) findViewById(R.id.rl_menu_on);
        rl_first_page = (RelativeLayout) findViewById(R.id.rl_first_page);
        rl_history = (RelativeLayout) findViewById(R.id.rl_history);
        rl_user = (RelativeLayout) findViewById(R.id.rl_user);
        rl_info = (RelativeLayout) findViewById(R.id.rl_info);
        rl_help = (RelativeLayout) findViewById(R.id.rl_help);
        rl_menu_content = (RelativeLayout) findViewById(R.id.rl_menu_content);

        input_access_pin = (EditText) findViewById(R.id.input_access_pin);


        // Clouds animation
        animate_clouds();

        //Ripple Effect for components

        //Start_Stop Button
        MaterialRippleLayout.on(b_start).rippleColor(getResources().getColor(R.color.colorAccent))
                .rippleAlpha(0.92f).rippleDuration(500)
                .create();

//        //Menu
//        MaterialRippleLayout.on(b_manage_access_pin).rippleColor(Color.WHITE)
//                .rippleAlpha(0.2f).rippleDuration(200).rippleRoundedCorners(140)
//                .create();

        //Manage friends
//        MaterialRippleLayout.on(b_manage_friends).rippleColor(Color.WHITE)
//                .rippleAlpha(0.2f).rippleDuration(200).rippleRoundedCorners(140)
//                .create();
//
//        MaterialRippleLayout.on(b_help_manage_friends).rippleColor(Color.WHITE)
//                .rippleAlpha(0.2f).rippleDuration(200).rippleRoundedCorners(140)
//                .create();

        //Get SharedPreferences
        sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();

        //Initialize App
        initialize();

        //Close Button listener
        b_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                if (isServiceRunning(UniClipService.class))
                    makeToast("Service will continue running in the background.");

            }

        });

//        //Manage friends Button listener
//        b_manage_friends.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                vibrate(50);
//
//                startActivity(new Intent(MainActivity.this, ManageFriendsActivity.class).putExtra("prev_screen", "main_screen_menu"));
//            }
//
//        });
//
//        //Manage friends from Help menu Button listener
//        b_help_manage_friends.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                vibrate(50);
//
//                startActivity(new Intent(MainActivity.this, ManageFriendsActivity.class).putExtra("prev_screen", "main_screen_menu"));
//            }
//
//        });

        //Close Button listener
        b_go_back_to_main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                b_start.setVisibility(View.VISIBLE);
                ed.putBoolean("authenticated", false).commit();

                b_start.setText("Start UniClip!");
                rl_home.startAnimation(fade_in);
                rl_home.setVisibility(View.VISIBLE);
                animate_clouds();

                rl_running.startAnimation(fade_out);
                rl_running.setVisibility(GONE);


                //Animate app title
                swingAnimate(findViewById(R.id.app_title), 700, 1000);

                sync_anim.setAlpha(0.00f);
                welcome_text.setText("UniClip is a multi-device clipboard synchronization " +
                        "application, which makes sharing texts, links, etc easy.");

                rl_menu_content.setVisibility(GONE);

                b_go_back_to_main.setVisibility(GONE);
            }
        });

        //Service start stop listener
        b_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //If service is not running and needs to be started
                if (!isServiceRunning(UniClipService.class)) {

                    sp_user_email = sp.getString("user_email", "unknown");

                    //Start Running activity
                    if(!sp_user_email.equals("deleted_user")){

                        // Start service
                        Intent intent = new Intent(MainActivity.this, UniClipService.class);
                        intent.putExtra("isAutorun", "false");
                        startService(intent);

                        //Log activity
                        Log.d(TAG, "Service started");

                        startActivity(new Intent(MainActivity.this, RunningActivity.class));
                    }
                    else{
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }

                    ed.putBoolean("authenticated", false).commit();
                    finish();
                }
            }

        });

        //Home menu buttons listener
        ((RelativeLayout)findViewById(R.id.rl_home_settings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                ((RelativeLayout) findViewById(R.id.rl_settings)).setVisibility(View.VISIBLE);
            }

        });



        ((Button)findViewById(R.id.settings_back)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_settings)).setVisibility(View.GONE);
            }

        });



        ((RelativeLayout)findViewById(R.id.rl_home_website)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse("http://piyushagade.xyz/uniclip"));
                startActivity(intent);
            }

        });

//        ((RelativeLayout)findViewById(R.id.rl_home_help)).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                vibrate(27);
//                Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse("http://piyushagade.xyz/uniclip"));
//                startActivity(intent);
//            }
//
//        });

        ((RelativeLayout)findViewById(R.id.rl_home_share)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Checkout this cool clipboard sharing app I have been using to get my productivity boosted. It lets you share clipboards amongst your devices, even between a mobile and a laptop.\n \nDownload the app here:\n http://piyushagade.xyz/uniclip\n");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_home_feedback)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_running_feedback)).setVisibility(View.VISIBLE);
            }

        });

        // Send feedback button listener
        ((Button)findViewById(R.id.b_send_feedback)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent emailIntent = new Intent( Intent.ACTION_SEND);

                emailIntent.setType("plain/text");

                emailIntent.putExtra(Intent.EXTRA_EMAIL,
                        new String[] { "piyushagade@gmail.com" });

                emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                        "UniClip user feedback");

                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        ((EditText) findViewById(R.id.fb_text)).getText().toString());

                startActivity(Intent.createChooser(
                        emailIntent, "Send feedback"));

                (findViewById(R.id.rl_running_feedback)).setVisibility(View.GONE);
            }

        });



        ((Button)findViewById(R.id.b_feedback_cancel)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_running_feedback)).setVisibility(View.GONE);
            }

        });

        ((RelativeLayout)findViewById(R.id.rl_home_ratings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName())));
                }
            }

        });

        //Tutorial actions
        ((RelativeLayout)findViewById(R.id.rl_home_tutorial)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                startActivity(new Intent(MainActivity.this, TutorialActivity.class));
            }

        });


        //Privacy Policy
        ((RelativeLayout)findViewById(R.id.rl_home_privacy)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                if(isNetworkAvailable()){
                    String url = "http://piyushagade.xyz/uniclip/download/uniclip_privacy_policy.pdf";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
                else{
                    makeSnack("Network unavailable.");
                }
            }

        });


        // Donate
        findViewById(R.id.b_donate).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String url = "https://www.paypal.me/piyushagade";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

                makeToast("Thank you! Today is the day I eat. :)");
            }
        });



        //Menu button listener
        b_menu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                vibrate(28);
                startActivity(new Intent(MainActivity.this, MenuActivity.class).putExtra("prev_activity", "main_activity"));

            }


        });




        //Autostart checkbox listener
        cb_autostart.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener()

                 {
                     @Override
                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                         if (isChecked) {
                             ed.putBoolean("autostart", true);
                         } else {
                             ed.putBoolean("autostart", false);
                         }
                         ed.commit();

                     }
                 }

                );

        //Notiication checkbox listener
        cb_notification.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener()

                 {
                     @Override
                     public void onCheckedChanged (CompoundButton buttonView,boolean isChecked){
                         if (isChecked) {
                             ed.putBoolean("notification", true);
                         } else {
                             ed.putBoolean("notification", false);
                         }
                         ed.commit();
                     }
                 }
                );

        //Open_URL checkbox listener
        cb_open_url.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener()

                 {
                     @Override
                     public void onCheckedChanged (CompoundButton buttonView,boolean isChecked){
                         if (isChecked) {
                             ed.putBoolean("open_url", true);
                         } else {
                             ed.putBoolean("open_url", false);
                         }
                         ed.commit();

                     }
                 }

                );

        //Vibrate checkbox listener
        cb_vibrate.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener()

                 {
                     @Override
                     public void onCheckedChanged (CompoundButton buttonView,boolean isChecked){
                         if (isChecked) {
                             ed.putBoolean("vibrate", true);
                         } else {
                             ed.putBoolean("vibrate", false);
                         }
                         ed.commit();

                     }
                 }

                );

        //Theme checkbox listener
        cb_theme.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener()

                 {
                     @Override
                     public void onCheckedChanged (CompoundButton buttonView,boolean isChecked){
                         if (isChecked) {
                             ed.putBoolean("theme", true);
                             rl_main.setBackgroundColor(colorPrimary);
                         } else {
                             ed.putBoolean("theme", false);
                             rl_main.setBackgroundColor(Color.parseColor("#DE111111"));
                         }
                         ed.commit();
                     }
                 }

                );

    }

    private void animate_clouds() {
        ImageView[] clouds_group1 = {(ImageView) findViewById(R.id.cloud3), (ImageView) findViewById(R.id.cloud4)};
        ImageView[] clouds_group2 = {(ImageView) findViewById(R.id.cloud1), (ImageView) findViewById(R.id.cloud2)};

        for(ImageView cloud : clouds_group1){
            cloud.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.cloud_animation_to_right));
        }


        for(ImageView cloud : clouds_group2){
            cloud.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.cloud_animation_to_left));
        }


        // Pulsator effect
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.setColor(Color.parseColor("#66FFFFFF"));
        pulsator.start();
    }


    private void unanimate_clouds() {
        ImageView[] clouds_group1 = {(ImageView) findViewById(R.id.cloud3), (ImageView) findViewById(R.id.cloud4)};
        ImageView[] clouds_group2 = {(ImageView) findViewById(R.id.cloud1), (ImageView) findViewById(R.id.cloud2)};

        for(ImageView cloud : clouds_group1){
            cloud.clearAnimation();
        }


        for(ImageView cloud : clouds_group2){
            cloud.clearAnimation();
        }
        pulsator.stop();
    }


    private Runnable refreshServiceStatus = new Runnable() {

        @Override
        public void run() {

            if(!stopRunnables) {
                sp_authenticated = sp.getBoolean("authenticated", false);

                //Service running and authenticated
                if (isServiceRunning(UniClipService.class) && sp_authenticated) {
                    if (isNetworkAvailable())
                        status_service.setText("Service:\n  Running. Listening to the cloudboard.");
                    else status_service.setText("Service:\n  Running. Waiting for network.");

                    //Service running and NOT authenticated
                } else if (isServiceRunning(UniClipService.class) && !sp_authenticated) {

                    //Waiting for authentication
                    if (isNetworkAvailable())
                        status_service.setText("Service:\n  Waiting for authentication.");
                    else {
                        status_service.setText("Service:\n  Not running. Waiting for network.");
                    }
                }

                //If service not running
                else {
                    status_service.setText("Service:\n  Error. Restart the application.");
                }
            }

            handler_status.postDelayed(this, refreshServiceStatusInterval);
        }
    };


    private Runnable refreshConnectionStatus = new Runnable() {
        @Override
        public void run() {

            if(!stopRunnables)
                if(isNetworkAvailable()){
                    status_connection.setText("Connection:\n  Connected to the server.");
                    b_diagnose.setVisibility(GONE);
                }
                else {
                    status_connection.setText("Connection:\n  Internet unavailable.");
                    b_diagnose.setVisibility(View.VISIBLE);
                }

            handler_connection.postDelayed(this, refreshConnectionStatusInterval);
        }
    };

    //Get Access Pin
    private void getAccessPin() {
        //Format email address (remove the .)
        String user_node = encrypt(encrypt(encrypt(sp.getString("user_email", "").replaceAll("\\.", ""))));

        //Firebase
        DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

        //Get access pin from firebase
        fb.child("key").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Set user access pin
                    try{
                        ed.putInt("access_pin", Integer.valueOf(snapshot.getValue().toString())).commit();
                    }catch (NumberFormatException nfe){
                        //Do nothing
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        fb = null;
    }



    //Check if a number is even
    private boolean isEven(int i) {
        if(i % 2 == 0) return true;
        return false;
    }


    //Initialize application
    private void initialize() {

        Log.d(TAG, "Initialize()");


        //Showcase UI
        mainShowcaseInitiate();

        //Get Color
        colorPrimary = getResources().getColor(R.color.colorPrimary);
        colorAccent = getResources().getColor(R.color.colorAccent);

        //Get Values SP
        sp_autostart = sp.getBoolean("autostart", true);
        sp_notification = sp.getBoolean("notification", true);
        sp_vibrate = sp.getBoolean("vibrate", true);
        sp_theme = sp.getBoolean("theme", true);
        sp_user_email = sp.getString("user_email", "unknown");
        sp_device_name = sp.getString("device_name", "unknown");
        sp_first_run = sp.getBoolean("first_run", true);
        sp_open_url = sp.getBoolean("open_url", true);
        sp_are_creator = sp.getBoolean("creator", false);
        sp_authenticated = sp.getBoolean("authenticated", false);
        sp_unread = sp.getInt("unread", 0);


        //Intro Screen
        if(sp_first_run){
            startActivity(new Intent(MainActivity.this, MainIntroActivity.class));

            Log.d(TAG, "Intro activity running.");
            finish();
        }

        //Detect accelerometer
        PackageManager manager = getPackageManager();
        boolean hasAccelerometer = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);

        if(!hasAccelerometer){
            sb_get_sensitivity.setEnabled(false);
            sb_get_numberShakes.setEnabled(false);
            sb_get_sensitivity.setProgress(0);

            sb_share_sensitivity.setEnabled(false);
            sb_share_numberShakes.setEnabled(false);
            sb_share_sensitivity.setProgress(0);
        }

        //Detect Vibrator
        String vs = Context.VIBRATOR_SERVICE;
        Vibrator mVibrator = (Vibrator)getSystemService(vs);

        boolean hasVibrator = mVibrator.hasVibrator();

        if(!hasVibrator){
            cb_vibrate.setChecked(false);
            cb_vibrate.setEnabled(false);
            sp_vibrate = false;
            ed.putBoolean("vibrate", false).commit();
        }

        //Get user email
        if(hasPermission() && sp_user_email.equals("unknown")) {
            AccountManager accountManager = AccountManager.get(MainActivity.this);
            Account account = getAccount(accountManager);

            if (account != null) {
                ed.putString("user_email", account.name);
                ed.commit();

                sp_user_email = sp.getString("user_email", "unknown");
            } else
                makeSnackForPermissions("Grant 'Contacts' permission to UniClip!");
        }

        else if(!hasPermission() && !sp_first_run){

            startActivity(new Intent(MainActivity.this, ActivityPermission.class));
            finish();
        }


        //Get device model
        ed.putString("device_name", getDeviceName());
        ed.commit();

        history_list_activity = new ArrayList<String>();
        syncHistoryLists();

        // Hide menu
        rl_menu_content.setVisibility(View.INVISIBLE);

        //Format email address (remove the .)
        String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));

        //Firebase
        DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

        //Get access pin
        getAccessPin();

        //Set up content if service is running when app is opened
        if (isServiceRunning(UniClipService.class)) {

            //Start Running activity
            startActivity(new Intent(MainActivity.this, RunningActivity.class));
            finish();

        }

        //Service not running
        else {
            b_start.setText("Start UniClip!");
            rl_home.setVisibility(View.VISIBLE);
            animate_clouds();
            sync_anim.setAlpha(0.00f);

            welcome_text.setText("UniClip is a multi-device clipboard synchronization " +
                    "application, which makes sharing texts, links, etc easy.");
        }

        //Initialize settings menu
        ((RelativeLayout) findViewById(R.id.rl_cb_lockscreen)).setVisibility(GONE);


        //Sync Icon Animation
        sync_anim.startAnimation(rotate);


        //Set Shake Sensitivity and number of Shakes
        sp_get_sensitivity = sp.getInt("get_sensitivity", 2+1);
        sp_get_shakes = sp.getInt("get_shakes", 0);
        sp_share_sensitivity = sp.getInt("share_sensitivity", 2+1);
        sp_share_shakes = sp.getInt("share_shakes", 3);


        //Set seekbar progresses
        sb_get_numberShakes.setProgress(sp_get_shakes);
        sb_get_sensitivity.setProgress(sp_get_sensitivity - 1);
        get_sensitivity_indicator.setText(String.valueOf(sp_get_sensitivity));
        get_shakes_indicator.setText(String.valueOf(sp_get_shakes));


        sb_share_numberShakes.setProgress(sp_share_shakes - 1);
        sb_share_sensitivity.setProgress(sp_share_sensitivity - 1);
        share_sensitivity_indicator.setText(String.valueOf(sp_share_sensitivity));
        share_shakes_indicator.setText(String.valueOf(sp_share_shakes));


        //Initialize Checkboxes
        if(sp_autostart)cb_autostart.setChecked(true);
        else cb_autostart.setChecked(false);

        if(sp_notification)cb_notification.setChecked(true);
        else cb_notification.setChecked(false);

        if(sp_vibrate)cb_vibrate.setChecked(true);
        else cb_vibrate.setChecked(false);

        if(sp_open_url)cb_open_url.setChecked(true);
        else cb_open_url.setChecked(false);

        if(sp_theme){
            cb_theme.setChecked(true);
            rl_main.setBackgroundColor(colorPrimary);
        }
        else{
            cb_theme.setChecked(false);
            rl_main.setBackgroundColor(Color.parseColor("#DE111111"));
        }

        //Seekbar OnChange Listeners
        sb_get_sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                get_sensitivity = progress;
                get_sensitivity_indicator.setText(String.valueOf(get_sensitivity + 1));
                ed.putInt("get_sensitivity", get_sensitivity + 1);
                ed.commit();
            }
        });


        sb_get_numberShakes.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        get_numberShakes = progress;
                        get_shakes_indicator.setText(String.valueOf(get_numberShakes));
                        ed.putInt("get_shakes", get_numberShakes);
                        ed.commit();

                        if(progress == 0)sb_get_sensitivity.setEnabled(false);
                        else sb_get_sensitivity.setEnabled(true);

                    }
                }
        );

        sb_share_sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                share_sensitivity = progress;
                share_sensitivity_indicator.setText(String.valueOf(share_sensitivity + 1));
                ed.putInt("share_sensitivity", share_sensitivity + 1);
                ed.commit();
            }
        });


        sb_share_numberShakes.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        share_numberShakes = progress;
                        share_shakes_indicator.setText(String.valueOf(share_numberShakes + 1));
                        ed.putInt("share_shakes", share_numberShakes + 1);
                        ed.commit();

                    }
                }
        );

        //Disable sensitivities if number of shakes
        if(sb_get_numberShakes.getProgress() == 0)sb_get_sensitivity.setEnabled(false);

        //Disable first_run flag
        ed.putBoolean("first_run", false).commit();
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

    //Mainscreen showcase
    private void mainShowcaseInitiate() {
        //Menu Button
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .setMaskColor(Color.parseColor("#66000000"))
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.MINIMUM)
                .setDelayMillis(100)
                .enableFadeAnimation(true)
                .performClick(false)
                .dismissOnTouch(true)
                .setInfoText("This is Menu button. It has user info, access pin, and clipboard history.")
                .setTarget(b_menu)
                .setUsageId("card_1")
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String materialIntroViewId) {
                        //Start/Stop button
                        new MaterialIntroView.Builder(MainActivity.this)
                                .enableDotAnimation(false)
                                .setMaskColor(Color.parseColor("#66000000"))
                                .setFocusGravity(FocusGravity.CENTER)
                                .setFocusType(Focus.NORMAL)
                                .setDelayMillis(100)
                                .enableFadeAnimation(true)
                                .performClick(false)
                                .dismissOnTouch(true)
                                .setInfoText("This button turns on the awesome.")
                                .setTarget(b_start)
                                .setUsageId("card_2")
                                .setListener(new MaterialIntroListener() {
                                    @Override
                                    public void onUserClicked(String materialIntroViewId) {
                                        //Settings
                                        new MaterialIntroView.Builder(MainActivity.this)
                                                .enableDotAnimation(false)
                                                .setMaskColor(Color.parseColor("#66000000"))
                                                .setFocusGravity(FocusGravity.CENTER)
                                                .setFocusType(Focus.ALL)
                                                .setDelayMillis(100)
                                                .enableFadeAnimation(true)
                                                .performClick(false)
                                                .dismissOnTouch(true)
                                                .setInfoText("These are all the settings and preferences.")
                                                .setTarget(rl_home)
                                                .setUsageId("card_3")
                                                .show();
                                    }
                                })
                                .show();
                    }
                })
                .show();


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
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
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

    //Get Account Name
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

    //Check if 'Accounts' permission is granted
    private boolean hasPermission()
    {
        String permission = "android.permission.GET_ACCOUNTS";
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    //Diagnose Method
    private void diagnose(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        TextView diagnosis = (TextView) findViewById(R.id.No_conn_diagnosis);

        //3G check
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        //WiFi Check
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();

        if (!is3g && !isWifi){
            diagnosis.setText("Both Wifi and Mobile data are off.");
        }
        else if (is3g && !isWifi){
            diagnosis.setText("Mobile data is on, Wifi is off. \nCheck if you are in network range.");
        }
        else if (!is3g && isWifi){
            diagnosis.setText("There seems to be a problem with Wifi. \nTry mobile data instead.");
        }
    }

    private void updateBadger() {
        sp_unread = sp.getInt("unread", 0);
        //ShortcutBadger
        ShortcutBadger.applyCount(getApplication(), sp_unread);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(MainActivity.this, QRActivity.class));

                        }
                    }, 2000);


                } else {

                }
            }
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

    public boolean validate_email(String email){
        Pattern ptr = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");
        return ptr.matcher(email).matches();

    }


}

