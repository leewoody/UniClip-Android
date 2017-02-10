package com.piyushagade.uniclip;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
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
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import java.util.List;
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
public class RunningActivity extends Activity {

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    private long refreshHistoryInterval = 4000;
    private long refreshConnectionStatusInterval = 6000;
    private long refreshServiceStatusInterval = 6000;
    private long refreshDevicesListInterval = 4000;

    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final String TAG = RunningActivity.class.getSimpleName();
    private boolean runRunnables = true;
    private RelativeLayout rl_noconnection;
    private DatabaseReference fb;
    private boolean user_created_now;


    public static int desktop_version_required = 3;
    public static int desktop_version_in_use;
    private TextView lockscreen_pin;
    private int keypad_num;

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
        if(handler_connection != null) handler_connection.removeCallbacks(connection_monitor);

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

    private Button b_stop, b_clear_history, b_manage_access_pin, b_set_access_pin, b_diagnose, b_go_back_to_main, b_manage_friends, b_help_manage_friends, b_enable_overlay;
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
    private boolean sp_autostart, sp_notification, sp_vibrate, sp_lockscreen_enabled, sp_theme, sp_first_run, sp_open_url, sp_are_creator, sp_authenticated;;
    private String sp_user_email, sp_device_name, sp_creator_device;
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

        setContentView(R.layout.activity_running);



        // Get the intent that started this activity
        Intent shareIntent = new Intent(this, UniClipService.class);
        // Get the action of the intent
        String action = shareIntent.getAction();
        // Get the type of intent (Text or Image)
        String type = shareIntent.getType();
        // When Intent's action is 'ACTION+SEND' and Tyoe is not null
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            // When type is 'text/plain'
            if ("text/plain".equals(type)) {
                handleSendText(shareIntent); // Handle text being sent
            }
        }




        //UI Components
        b_stop = (Button) findViewById(R.id.b_stop);
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

        //Ripple Effect for components

        //Start_Stop Button
        MaterialRippleLayout.on(b_stop).rippleColor(getResources().getColor(R.color.colorAccent))
                .rippleAlpha(0.92f).rippleDuration(500)
                .create();

        //Menu
        MaterialRippleLayout.on(b_manage_access_pin).rippleColor(Color.WHITE)
                .rippleAlpha(0.2f).rippleDuration(200).rippleRoundedCorners(140)
                .create();

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

        //Close Button listener
        b_go_back_to_main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                b_stop.setVisibility(View.VISIBLE);
                ed.putBoolean("authenticated", false).commit();

                b_stop.setText("Start UniClip!");
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

        //Notify if new user account is created now listener // Close if account was deleted
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Boolean new_account_created = Boolean.valueOf(intent.getBooleanExtra("new_account_created", false));
                        Boolean account_deleted = Boolean.valueOf(intent.getBooleanExtra("account_deleted", false));

                        if(new_account_created) {
                            ed.putBoolean("user_created_now", true).commit();
                            findViewById(R.id.rl_new_account).setVisibility(View.VISIBLE);

                            sp_user_email = sp.getString("user_email", "Error");
                            ((TextView) findViewById(R.id.new_account_desc)).setText("Congratulations, your brand new UniClip account is now active. \n\nYour username is:\n" + sp_user_email);


                            //Disable lockscreen
                            findViewById(R.id.rl_update_desktop_client).setVisibility(GONE);
                            ed.putBoolean("lockscreen_enabled", false).commit();
                            ((CheckBox) findViewById(R.id.cb_lockscreen)).setChecked(false);

                            ed.putBoolean("user_created_now", false).commit();
                        }

                        if(account_deleted){
                            ed.putString("user_email", "deleted_user").commit();
                            finish();
                        }

                    }
                }, new IntentFilter(UniClipService.class.getName())
        );

        findViewById(R.id.b_new_account_sweet).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                findViewById(R.id.rl_new_account).setVisibility(View.GONE);
                vibrate(50);
            }
        });

        findViewById(R.id.b_new_account_privacy).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String url = "http://piyushagade.xyz/uniclip/download/uniclip_privacy_policy.pdf";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                vibrate(50);
            }
        });

        // Social Message Action Button
        findViewById(R.id.b_social_message).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String url = "https://www.antiwar.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });


        // Donate
        findViewById(R.id.b_donate).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(50);
                String url = "https://www.paypal.me/piyushagade";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

                makeToast("Thank you! Today is the day I eat. :)");
            }
        });


        //Service stop listener
        b_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                (findViewById(R.id.rl_email_verification)).setVisibility(View.GONE);
                (findViewById(R.id.rl_revalidate)).setVisibility(View.GONE);
                (findViewById(R.id.rl_validate)).setVisibility(View.GONE);


                //Stop unnecessary runnables
                stopRunnables();


                //Reset Shortcut Badger
                ed.putInt("unread", 0).commit();
                updateBadger();

                //Stop Service
                stopService(new Intent(getBaseContext(), UniClipService.class));
                b_stop.setVisibility(View.VISIBLE);
                ed.putBoolean("authenticated", false).commit();

                //Start Home activity
                startActivity(new Intent(RunningActivity.this, MainActivity.class));
                finish();

                vibrate(80);
                Log.d("RunningActivity", "Service stopped.");


            }

        });

        //Manage Access Pin Button listener
        b_manage_access_pin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(28);

                getAccessPin();
                int current_pin = sp.getInt("access_pin", 0);

                findViewById(R.id.rl_running_manage_pin).setVisibility(View.VISIBLE);

            }

        });

        //Change Access Pin Button listener
        findViewById(R.id.b_change_pin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(28);

                getAccessPin();
                String current_pin = String.valueOf(sp.getInt("access_pin", 0));
                String new_pin = ((EditText)findViewById(R.id.running_change_pin_new_pin)).getText().toString();

                if(new_pin.length()==4 && !new_pin.equals(current_pin)) {
                    sp_user_email = sp.getString("user_email", "unknown");

                    //Format email address (remove the .)
                    String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));

                    //Firebase
                    DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);
                    fb.child("key").setValue(new_pin);

                    //Commit to storage
                    ed.putInt("access_pin", Integer.valueOf(new_pin)).commit();

                    //Set new PIN to all displays shoeing PIN
                    ((TextView) findViewById(R.id.running_adddevice_menu_pin)).setText(new_pin);
                    ((TextView) findViewById(R.id.cb_lockscreen_pin)).setText(new_pin);
                    ((TextView) findViewById(R.id.running_manage_pin_pin)).setText(new_pin);
                }
                else if(new_pin.length() != 4) makeToast("Enter a valid 4 digit PIN");
                else if(new_pin.equals(current_pin)) makeToast("Enter a PIN that is not your current PIN");


            }

        });

        //Change Access Pin back Button listener
        findViewById(R.id.running_change_pin_back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(28);
                findViewById(R.id.rl_running_manage_pin).setVisibility(View.GONE);

            }

        });

        //Running layout buttons
        ((Button)findViewById(R.id.b_add_devices)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_running_adddevice_menu)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.running_adddevice_menu_pin)).setText(String.valueOf(sp.getInt("access_pin", 0)));
            }

        });

        ((Button)findViewById(R.id.running_adddevice_menu_back)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_running_adddevice_menu)).setVisibility(View.GONE);
            }

        });

        ((Button)findViewById(R.id.b_feedback_cancel)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_running_feedback)).setVisibility(View.GONE);
            }

        });

        ((Button)findViewById(R.id.b_user_switch)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_running_user_menu)).setVisibility(View.VISIBLE);
            }

        });

        ((Button)findViewById(R.id.running_user_menu_back)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_running_user_menu)).setVisibility(View.GONE);
                ((Button)findViewById(R.id.b_del_user)).setText("Unregister");
                ((Button)findViewById(R.id.b_change_creator)).setText("Change Creator");
            }

        });


        ((Button)findViewById(R.id.b_show_settings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_settings)).setVisibility(View.VISIBLE);
            }

        });

        ((Button)findViewById(R.id.settings_back)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((RelativeLayout) findViewById(R.id.rl_settings)).setVisibility(View.GONE);
            }

        });

        ((Button)findViewById(R.id.b_show_tutorial)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(RunningActivity.this, TutorialActivity.class));
            }

        });

        ((Button)findViewById(R.id.b_privacy)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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


        //Update desktop client
        findViewById(R.id.b_update_desktop_client).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                int key = (int) new Random().nextInt(9999);
                if (key < 1000) key = key * 10 + new Random().nextInt(99);
                else if (key < 100) key = key * 100 + new Random().nextInt(9);

                sp_user_email = sp.getString("user_email", "unknown");

                //Format email address (remove the .)
                String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));

                //Firebase
                DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);
                fb.child("link").setValue("http://www.piyushagade.xyz/uniclip"+'%'+key);

                makeToast("The download page will open on your desktop shortly.");

            }
        });


        //Update desktop client dismiss
        findViewById(R.id.b_update_desktop_client_dismiss).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                findViewById(R.id.rl_update_desktop_client).setVisibility(GONE);


            }
        });


        //Delete user account button listener
        findViewById(R.id.b_del_user).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                String button_label = ((Button)findViewById(R.id.b_del_user)).getText().toString();

                if(button_label.equals("Are you sure?")) {
                    //Stop service
                    stopService(new Intent(getBaseContext(), UniClipService.class));

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            sp_user_email = sp.getString("user_email", "unknown");

                            //Format email address (remove the .)
                            String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));

                            //Firebase
                            DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);
                            fb.child("key").setValue(0);

//                            fb.setValue(null);

                            makeToast(sp_user_email + " account terminated by user.");

                            //Persist the fact that the user account was deleted
                            ed.putString("user_email", "deleted_user").commit();

                            startActivity(new Intent(RunningActivity.this, MainActivity.class));
                            finish();
                        }
                    }, 600);
                }
                else if(button_label.equals("Unregister")){
                    vibrate(100);
                    ((Button)findViewById(R.id.b_del_user)).setText("Are you sure?");
                }

            }
        });


        //Change creator button listener
        findViewById(R.id.b_change_creator).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                String button_label = ((Button)findViewById(R.id.b_change_creator)).getText().toString();

                if(button_label.equals("Are you sure?")) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            sp_user_email = sp.getString("user_email", "unknown");
                            sp_device_name = sp.getString("device_name", "unknown");
                            sp_creator_device = sp.getString("creator_device","Error");

                            if(!sp_device_name.equals(sp_creator_device)) {
                                //Format email address (remove the .)
                                String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));

                                //Firebase
                                DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

                                //Change creator device
                                fb.child("creator").setValue(sp_device_name);

                                makeToast("This is your new 'Creator Device'.");

                                ((RelativeLayout)findViewById(R.id.rl_running_user_menu)).setVisibility(GONE);

                                ed.putBoolean("creator", true).commit();
                                ed.putString("creator_device", sp_device_name).commit();
                                initialize();
                            }
                            else makeToast("This is already your 'Creator Device'");

                            ((Button)findViewById(R.id.b_change_creator)).setText("Change Creator");
                        }
                    }, 600);
                }
                else if(button_label.equals("Change Creator")){
                    vibrate(100);

                    ((Button)findViewById(R.id.b_change_creator)).setText("Are you sure?");
                }

            }
        });


        // User account switch
        (findViewById(R.id.running_user_switch)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Log.d(TAG, "Switch user button pressed.");

                // If email entered is a valid email address
                if(!((EditText) findViewById(R.id.running_user_menu_new_email)).getText().toString().equals("") &&
                        validate_email(((EditText) findViewById(R.id.running_user_menu_new_email)).getText().toString())) {

                    final String new_email = ((EditText) findViewById(R.id.running_user_menu_new_email)).getText().toString().toLowerCase();
                    //Format email address (remove the .)
                    String user_node = encrypt(encrypt(encrypt(new_email.replaceAll("\\.", ""))));

                    //Firebase
                    fb = mRootRef.child("cloudboard").child(user_node);


                    //Check if this device is creator / Listen for change in 'Creator Device'
                    fb.child("creator").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                                if(sp_device_name.equals(snapshot.getValue().toString())){
                                    ed.putBoolean("creator", true).commit();
                                }
                                else{
                                    ed.putBoolean("creator", false).commit();
                                }

                                ed.putString("creator_device", snapshot.getValue().toString()).commit();

                                initialize();

                                ((TextView)findViewById(R.id.re_creator_device)).setText(snapshot.getValue().toString());

                                ((TextView) findViewById(R.id.creator_device)).setText(snapshot.getValue().toString());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });


                    //Listen for change in Access PIN
                    fb.child("key").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String new_pin =  snapshot.getValue().toString();
                                ed.putString("access_pin", new_pin).commit();


                                //Set new PIN to all displays shoeing PIN
                                ((TextView) findViewById(R.id.running_adddevice_menu_pin)).setText(new_pin);
                                ((TextView) findViewById(R.id.cb_lockscreen_pin)).setText(new_pin);
                                ((TextView) findViewById(R.id.running_manage_pin_pin)).setText(new_pin);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });

                    // Check if user exists / Mail verification code if user doesnt exists
                    fb.child("key").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {

                            //If user doesn't exists, and a new account needs to be created and be verified
                            if (!snapshot.exists()) {
                                ed.putString("new_email", new_email).commit();

                                //Send confirmation email with PIN
                                String to = new_email;
                                String subject = "UniClip account verification";
                                int verification_code  = new Random().nextInt(99999);
                                ed.putInt("switch_verification_code", verification_code).commit();

                                String body = "Your UniClip verification code: " + verification_code;


                                //Creating SendMail object
                                Mail sm = new Mail(RunningActivity.this, to, subject , body);
                                sm.execute();

                                (findViewById(R.id.rl_email_verification)).setVisibility(View.VISIBLE);
                                (findViewById(R.id.rl_authenticated_running)).setVisibility(View.GONE);

                            }
                            // If account already created and verified
                            else{

                                //Check if device is creator
                                fb.child("creator").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        if (snapshot.exists()) {

                                            if(sp_device_name.equals(snapshot.getValue().toString())){
                                                ed.putBoolean("creator", true).commit();
                                                sp_are_creator = true;

                                                ed.putString("user_email", new_email).commit();
                                                ((TextView) findViewById(R.id.running_user_email)).setText(new_email);
                                                CharSequence str = ((TextView) findViewById(R.id.new_account_desc)).getText();
                                                ((TextView) findViewById(R.id.new_account_desc)).setText("Congratulations, your brand new UniClip account is now active. \n\nYour username is:\n"+new_email);

                                                //Restart Service
                                                stopService(new Intent(getBaseContext(), UniClipService.class));
                                                Intent intent = new Intent(RunningActivity.this, UniClipService.class);
                                                intent.putExtra("isAutorun", "false");
                                                startService(intent);

                                                initialize();

                                                (findViewById(R.id.rl_running_user_menu)).setVisibility(View.GONE);

                                                makeSnack("Account switched to " + sp.getString("user_email", "error"));

                                                (findViewById(R.id.g_access_pin)).setVisibility(View.VISIBLE);


                                                Log.d("ABC", "New account's creator device. " + new_email);


                                            }
                                            else{
                                                ed.putBoolean("creator", false).commit();
                                                sp_are_creator = false;

                                                b_set_access_pin.setEnabled(true);
                                                b_set_access_pin.setText(">");

                                                ed.putString("new_email", new_email).commit();

                                                (findViewById(R.id.rl_revalidate)).setVisibility(View.VISIBLE);
                                                (findViewById(R.id.rl_authenticated_running)).setVisibility(View.GONE);
                                                (findViewById(R.id.g_access_pin)).setVisibility(View.GONE);

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                    }
                                });

                                findViewById(R.id.rl_new_account).setVisibility(View.GONE);

                                // Get Access PIN
                                fb.child("key").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            ed.putInt("access_pin", Integer.valueOf(snapshot.getValue().toString())).commit();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });

                    (findViewById(R.id.rl_running_user_menu)).setVisibility(View.GONE);

                    //Disable lockscreen
                    findViewById(R.id.rl_lockscreen).setVisibility(GONE);
                    ed.putBoolean("lockscreen_enabled", false).commit();
                    ((CheckBox)findViewById(R.id.cb_lockscreen)).setChecked(false);

                    getAccessPin();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.cb_lockscreen_pin)).setText("Remember, PIN is: " + String.valueOf(sp.getInt("access_pin", 0)));
                            ((TextView) findViewById(R.id.running_manage_pin_pin)).setText(String.valueOf(sp.getInt("access_pin", 0)));

                        }
                    }, 800);
                }
                // If entered email ID is invalid
                else{
                    makeToast("Enter a valid email ID.");
                }
            }

        });




        //Show feedback form button listener
        (findViewById(R.id.b_show_feedback_form)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                (findViewById(R.id.rl_running_feedback)).setVisibility(View.VISIBLE);
            }
        });

        // Send feedback button listener
        (findViewById(R.id.b_send_feedback)).setOnClickListener(new View.OnClickListener() {
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





        //Menu button listener
        b_menu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(28);
                startActivity(new Intent(RunningActivity.this, MenuActivity.class));
            }


        });



        //Running history button listener
        (findViewById(R.id.b_show_history)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                startActivity(new Intent(RunningActivity.this, MenuActivity.class).putExtra("prev_activity", "running_activity"));

            }
        });

        //Empty listeners
        (findViewById(R.id.rl_running_adddevice_menu)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }
        });

        (findViewById(R.id.rl_running_user_menu)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }
        });

        (findViewById(R.id.rl_settings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing
            }
        });

        (findViewById(R.id.rl_running_user_menu_mask)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                (findViewById(R.id.rl_running_user_menu)).setVisibility(View.GONE);
                ((Button)findViewById(R.id.b_del_user)).setText("Unregister");
                ((Button)findViewById(R.id.b_change_creator)).setText("Change Creator");
            }
        });

        (findViewById(R.id.rl_running_adddevice_menu_mask)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                (findViewById(R.id.rl_running_adddevice_menu)).setVisibility(View.GONE);
            }
        });

        (findViewById(R.id.rl_running_settings_menu_mask)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                (findViewById(R.id.rl_settings)).setVisibility(View.GONE);
            }
        });

        (findViewById(R.id.rl_running_feedback_mask)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                (findViewById(R.id.rl_running_feedback)).setVisibility(View.GONE);
            }
        });

        (findViewById(R.id.rl_running_change_pin_mask)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                (findViewById(R.id.rl_running_manage_pin)).setVisibility(View.GONE);
            }
        });


        //Decode QR
        decode_qr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                if (ActivityCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(RunningActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);

                    return;
                }
                else{
                    if(isNetworkAvailable())
                        startActivity(new Intent(RunningActivity.this, QRActivity.class));
                    else
                        makeSnack("Internet not available.");
                }

            }
        });

        //Validate access pin button listener
        b_set_access_pin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);


                Log.d(TAG, "Set access PIN button pressed.");

                if(!isNetworkAvailable()){
                    makeSnack("Network unavailable");
                }
                else {
                    b_set_access_pin.setText(">");
                }

                final String input_pin = input_access_pin.getText().toString();


                Log.d(TAG, "PIN input: " + input_pin + " against " + sp.getString("user_email", "unknown"));

                sp_user_email = sp.getString("user_email", "unknown");

                //Format email address (remove the .)
                String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));



                //Firebase
                DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

                fb.child("key").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            Log.d(TAG, "Correct PIN: " + snapshot.getValue().toString());

                            // if Correct Access Pin
                            if (input_pin.equals(snapshot.getValue().toString())) {
                                (findViewById(R.id.rl_validate)).setVisibility(GONE);
                                (findViewById(R.id.rl_authenticated_running)).setVisibility(View.VISIBLE);
                                b_set_access_pin.setText(">");
                                b_set_access_pin.setEnabled(true);

                                b_stop.setVisibility(View.VISIBLE);

                                b_go_back_to_main.setVisibility(GONE);

                                sync_anim.startAnimation(rotate);
                                ed.putBoolean("authenticated", true).commit();
                                ed.putInt("access_pin", Integer.valueOf(snapshot.getValue().toString())).commit();

                                //Restart Service
                                stopService(new Intent(getBaseContext(), UniClipService.class));
                                Intent intent = new Intent(RunningActivity.this, UniClipService.class);
                                intent.putExtra("isAutorun", "false");
                                startService(intent);

                            }
                            else {
                                makeToast("Wrong Access Pin. Try Again");

                                //Animate input on wrong password
                                swingAnimate(findViewById(R.id.input_access_pin), 600, 300);

                                ed.putBoolean("authenticated", false).commit();
                                b_set_access_pin.setText(">");

                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });

            }
        });

        //Revalidate access pin button listener
        findViewById(R.id.re_b_set_access_pin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                Log.d("MainActivity", "Revalidate button pressed.");

                if(!isNetworkAvailable()){
                    makeSnack("Network unavailable");
                }
                else {
                    b_set_access_pin.setText(">");
                }

                final String input_pin = ((EditText) findViewById(R.id.re_input_access_pin)).getText().toString();

                //Format email address (remove the .)
                String user_node = encrypt(encrypt(encrypt(sp.getString("new_email", "unknown").replaceAll("\\.", ""))));

                //Firebase
                DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

                int correct_pin = sp.getInt("access_pin", 0);

                //Correct Access Pin
                if (input_pin.equals(String.valueOf(correct_pin))){

                    ed.putString("user_email", sp.getString("new_email", "unknown")).commit();
                    ed.putString("new_email", "none").commit();

                    (findViewById(R.id.rl_revalidate)).setVisibility(GONE);
                    (findViewById(R.id.rl_authenticated_running)).setVisibility(View.VISIBLE);

                    ((TextView) findViewById(R.id.running_user_email)).setText(sp.getString("user_email", "Error"));

                    makeSnack("Account switched to " + sp.getString("user_email", "error"));

                    b_stop.setVisibility(View.VISIBLE);

                    b_go_back_to_main.setVisibility(GONE);

                    sync_anim.startAnimation(rotate);
                    ed.putBoolean("authenticated", true).commit();

                    //Restart Service
                    stopService(new Intent(getBaseContext(), UniClipService.class));
                    Intent intent = new Intent(RunningActivity.this, UniClipService.class);
                    intent.putExtra("isAutorun", "false");
                    startService(intent);
                }
                else {
                    makeToast("Wrong Access Pin. Try Again");

                    //Animate input on wrong password
                    swingAnimate(findViewById(R.id.re_input_access_pin), 600, 300);

                    ed.putBoolean("authenticated", false).commit();

                }

            }
        });



        //Verify email button listener
        findViewById(R.id.b_verify_email).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                Log.d(TAG, "Verify email  button pressed.");

                if(!isNetworkAvailable()){
                    makeSnack("Network unavailable");
                }
                else {
                    b_set_access_pin.setText(">");
                }

                final String input_pin = ((EditText) findViewById(R.id.verify_email_code)).getText().toString();
                int correct_pin = sp.getInt("switch_verification_code", 0);

                //Correct Verification code
                if (input_pin.equals(String.valueOf(correct_pin))){

                    ed.putString("user_email", sp.getString("new_email", "unknown")).commit();
                    ed.putString("new_email", "none").commit();

                    (findViewById(R.id.rl_email_verification)).setVisibility(GONE);
                    (findViewById(R.id.rl_authenticated_running)).setVisibility(View.VISIBLE);

                    ((TextView) findViewById(R.id.running_user_email)).setText(sp.getString("user_email", "Error"));

                    makeSnack("Account verified: " + sp.getString("user_email", "error"));

                    b_stop.setVisibility(View.VISIBLE);

                    b_go_back_to_main.setVisibility(GONE);

                    sync_anim.startAnimation(rotate);
                    ed.putBoolean("authenticated", true).commit();

                    //Restart Service
                    stopService(new Intent(getBaseContext(), UniClipService.class));
                    Intent intent = new Intent(RunningActivity.this, UniClipService.class);
                    intent.putExtra("isAutorun", "false");
                    startService(intent);
                }
                //Wrong verification code entered
                else {
                    makeToast("Wrong verification code.");

                    //Animate input on wrong password
                    swingAnimate(findViewById(R.id.verify_email_code), 600, 300);

                    ed.putBoolean("authenticated", false).commit();

                }

            }
        });


        // Share UniClip! intent
        findViewById(R.id.b_running_share).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Checkout this cool clipboard sharing app I have been using to get my productivity boosted. It lets you share clipboards amongst your devices, even between a mobile and a laptop.\n \nDownload the app here:\n http://piyushagade.xyz/uniclip\n");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });


        // Verify new email
        findViewById(R.id.b_verify_email_resend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);


                Log.d("MainActivity", "Resend button pressed.");

                String to = sp.getString("new_email", "");
                String subject = "UniClip account verification";
                int verification_code  = new Random().nextInt(99999);
                ed.putInt("switch_verification_code", verification_code).commit();

                String body = "Your UniClip verification code: " + verification_code;


                //Creating SendMail object
                Mail sm = new Mail(RunningActivity.this, to, subject , body);
                sm.execute();

                findViewById(R.id.b_verify_email_resend).setVisibility(View.INVISIBLE);

            }
        });


        // Forgot PIN
        findViewById(R.id.b_forgot_pin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);


                Log.d(TAG, "Forgot PIN button pressed.");

                final String to = sp.getString("user_email", "");
                final String subject = "UniClip account Access PIN";


                //Format email address (remove the .)
                String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));

                //Firebase
                DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

                fb.child("key").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ed.putInt("access_pin", Integer.valueOf(snapshot.getValue().toString())).commit();

                            String body = "Your UniClip Access PIN: " + snapshot.getValue().toString();

                            //Creating SendMail object
                            Mail sm = new Mail(RunningActivity.this, to, subject , body);
                            sm.execute();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }
        });


        // Revalidate Forgot PIN
        findViewById(R.id.re_b_forgot_pin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);


                Log.d("MainActivity", "Revalidate Forgot PIN button pressed.");

                final String to = sp.getString("new_email", "");
                final String subject = "UniClip account Access PIN";


                //Format email address (remove the .)
                String user_node = encrypt(encrypt(encrypt(sp.getString("new_email", "").replaceAll("\\.", ""))));

                //Firebase
                DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

                fb.child("key").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ed.putInt("access_pin", Integer.valueOf(snapshot.getValue().toString())).commit();

                            String body = "Your UniClip Access PIN: " + snapshot.getValue().toString();

                            //Creating SendMail object
                            Mail sm = new Mail(RunningActivity.this, to, subject , body);
                            sm.execute();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }
        });





        //Grant overlay permission button listener
        b_enable_overlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(!Settings.canDrawOverlays(RunningActivity.this)){
                        makeToast("Enable UniClip to draw over other apps.");

                        Intent settings_intent = new Intent();
                        settings_intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        settings_intent.setData(uri);
                        startActivity(settings_intent);

                        b_enable_overlay.setText("Verify");
                    }
                    else{
                        //Verified
                        RelativeLayout rl_overlay_permission = (RelativeLayout) findViewById(R.id.rl_overlay_permission);
                        rl_overlay_permission.setVisibility(GONE);
                        makeSnack("Permission granted.");
                        b_enable_overlay.setText("Grant Permission");
                    }
                }
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

        //Lockscreen checkbox listener
        ((CheckBox)findViewById(R.id.cb_lockscreen)).setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener()

                 {
                     @Override
                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                         if (isChecked) {
                             ed.putBoolean("lockscreen_enabled", true);
                         } else {
                             ed.putBoolean("lockscreen_enabled", false);
                         }
                         ed.commit();

                     }
                 }

                );





        //Lockcreen button listener
        lockscreen_pin = (TextView)findViewById(R.id.lockscreen_pin);

        TextView [] keypad_array = {(TextView)findViewById(R.id.keypad_1), (TextView)findViewById(R.id.keypad_2), (TextView)findViewById(R.id.keypad_3), (TextView)findViewById(R.id.keypad_4),
                (TextView)findViewById(R.id.keypad_5), (TextView)findViewById(R.id.keypad_6), (TextView)findViewById(R.id.keypad_7), (TextView)findViewById(R.id.keypad_8), (TextView)findViewById(R.id.keypad_9), (TextView)findViewById(R.id.keypad_0)};


        for(TextView keypad_var : keypad_array) {
            final String val = (keypad_var).getText().toString();
            keypad_var.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    vibrate(50);

                    if(lockscreen_pin.getText().toString().length() < 9)
                        lockscreen_pin.append(val + " ");

                }
            });
        }

        findViewById(R.id.keypad_clear).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(50);
                lockscreen_pin.setText("");

            }
        });

        findViewById(R.id.keypad_enter).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(50);

                if(lockscreen_pin.getText().toString().replaceAll("\\s","").equals(String.valueOf(sp.getInt("access_pin", 0)))){
                    findViewById(R.id.rl_lockscreen).setVisibility(GONE);
                    lockscreen_pin.setText("");
                }else{
                    makeToast("Wrong PIN.");
                }

            }
        });

        findViewById(R.id.rl_lockscreen_mask).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Do nothing

            }
        });


        findViewById(R.id.lockscreen_forgot_pin).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(50);

                final String to = sp.getString("user_email", "");
                final String subject = "UniClip account Access PIN";
                String body = "Your UniClip Access PIN: " + String.valueOf(sp.getInt("access_pin", 0));

                //Creating SendMail object
                Mail sm = new Mail(RunningActivity.this, to, subject , body);
                sm.execute();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        makeToast("PIN has been sent to " + to);
                    }
                }, 1000);

            }
        });

    }

    private void animate_clouds() {
        ImageView[] clouds_group1 = {(ImageView) findViewById(R.id.cloud3), (ImageView) findViewById(R.id.cloud4)};
        ImageView[] clouds_group2 = {(ImageView) findViewById(R.id.cloud1), (ImageView) findViewById(R.id.cloud2)};

        for(ImageView cloud : clouds_group1){
            cloud.startAnimation(AnimationUtils.loadAnimation(RunningActivity.this, R.anim.cloud_animation_to_right));
        }


        for(ImageView cloud : clouds_group2){
            cloud.startAnimation(AnimationUtils.loadAnimation(RunningActivity.this, R.anim.cloud_animation_to_left));
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

    private Runnable connection_monitor = new Runnable() {
        @Override
        public void run() {

            if(isNetworkAvailable()){
                (findViewById(R.id.rl_no_connection)).setVisibility(View.GONE);
            }
            else {
                (findViewById(R.id.rl_no_connection)).setVisibility(View.VISIBLE);
                Log.d("MainActivity", "Connection not available");
                diagnose();
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
                    ed.putInt("access_pin", Integer.valueOf(snapshot.getValue().toString())).commit();
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

        Log.d(TAG, "Initializing Activity");

        //Get Color
        colorPrimary = getResources().getColor(R.color.colorPrimary);
        colorAccent = getResources().getColor(R.color.colorAccent);

        //Get Values SP
        sp_autostart = sp.getBoolean("autostart", true);
        sp_notification = sp.getBoolean("notification", true);
        sp_vibrate = sp.getBoolean("vibrate", true);
        sp_theme = sp.getBoolean("theme", true);
        sp_lockscreen_enabled = sp.getBoolean("lockscreen_enabled", false);
        sp_user_email = sp.getString("user_email", "unknown");
        sp_device_name = sp.getString("device_name", "unknown");
        sp_first_run = sp.getBoolean("first_run", true);
        sp_open_url = sp.getBoolean("open_url", true);
        sp_are_creator = sp.getBoolean("creator", false);
        sp_authenticated = sp.getBoolean("authenticated", false);
        sp_unread = sp.getInt("unread", 0);

        //Show lockscreen if required

        if(sp_lockscreen_enabled){
            findViewById(R.id.rl_lockscreen).setVisibility(View.VISIBLE);
        }

        //Set running scree info
        ((TextView) findViewById(R.id.running_user_email)).setText(sp_user_email);
        ((TextView) findViewById(R.id.running_add_device)).setText(sp_device_name);

        getAccessPin();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.cb_lockscreen_pin)).setText("Remeber, PIN is: " + String.valueOf(sp.getInt("access_pin", 0)));
                ((TextView) findViewById(R.id.running_manage_pin_pin)).setText(String.valueOf(sp.getInt("access_pin", 0)));

            }
        }, 800);


        //Overlay permission
        RelativeLayout rl_overlay_permission = (RelativeLayout) findViewById(R.id.rl_overlay_permission);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(RunningActivity.this)){
                rl_overlay_permission.setVisibility(View.VISIBLE);
            }else{
                rl_overlay_permission.setVisibility(GONE);
            }
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
            AccountManager accountManager = AccountManager.get(RunningActivity.this);
            Account account = getAccount(accountManager);

            if (account != null) {
                ed.putString("user_email", account.name);
                ed.commit();
                sp_user_email = sp.getString("user_email", "unknown");
            } else
                makeSnackForPermissions("Grant 'Contacts' permission to UniClip!");
        }

        else if(!hasPermission() && !sp_first_run){
            startActivity(new Intent(RunningActivity.this, ActivityPermission.class));
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


        //Notify if user account was created now
        if(sp.getBoolean("user_created_now", false)) {
            Log.d("content", "user account created now.");
            findViewById(R.id.rl_new_account).setVisibility(View.VISIBLE);

            sp_user_email = sp.getString("user_email", "Error");
            ((TextView) findViewById(R.id.new_account_desc)).setText("Congratulations, your brand new UniClip account is now active. \n\nYour username is:\n" + sp_user_email);

            //Disable lockscreen
            findViewById(R.id.rl_lockscreen).setVisibility(GONE);
            ed.putBoolean("lockscreen_enabled", false).commit();
            ((CheckBox)findViewById(R.id.cb_lockscreen)).setChecked(false);

            ed.putBoolean("user_created_now", false).commit();
        }

        //Desktop client version monitor
        DatabaseReference fb_devices = mRootRef.child("cloudboard").child(encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", "")))) + "/devices");

        fb_devices.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                int i = 1;    //Serial number
                if (snapshot.getChildrenCount() != 0)
                    for (final DataSnapshot postSnapshot : snapshot.getChildren()) {

                        try {
                            desktop_version_in_use = Integer.valueOf(postSnapshot.getValue().toString().split("%")[1]);

                            ed.putInt("desktop_version_in_use", desktop_version_in_use).commit();
                            ed.putInt("desktop_version_required", desktop_version_required).commit();

                            if (desktop_version_in_use < desktop_version_required && desktop_version_in_use > 0)
                                findViewById(R.id.rl_update_desktop_client).setVisibility(View.VISIBLE);
                            else{
                                findViewById(R.id.rl_update_desktop_client).setVisibility(View.GONE);
                            }

                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            //Do Nothing
                        }

                    }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        //Check if this device is creator
        fb.child("creator").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    if(sp_device_name.equals(snapshot.getValue().toString())){
                        ed.putBoolean("creator", true).commit();
                        sp_are_creator = true;

                        sp_authenticated = sp.getBoolean("authenticated", false);

                        //Set content
                        if (sp_authenticated) {
                            ((RelativeLayout) findViewById(R.id.g_access_pin)).setVisibility(View.VISIBLE);
                            Log.d("content", "Content for creator set up.");
                        }
                    }
                    else{
                        ed.putBoolean("creator", false).commit();
                        sp_are_creator = false;

                        //Set content
                        Log.d("content", "Content for slave set up.");
                        if (sp_authenticated) {
                            b_set_access_pin.setVisibility(View.VISIBLE);
                            b_set_access_pin.setText(">");

                            ((RelativeLayout) findViewById(R.id.g_access_pin)).setVisibility(View.GONE);

                            b_set_access_pin.setEnabled(true);
                            ((RelativeLayout)findViewById(R.id.rl_validate)).setVisibility(View.GONE);
                            ((RelativeLayout)findViewById(R.id.rl_authenticated_running)).setVisibility(View.VISIBLE);
                            input_access_pin.setVisibility(View.VISIBLE);

                        }
                        else if (!sp_authenticated) {

                            ((RelativeLayout) findViewById(R.id.g_access_pin)).setVisibility(View.GONE);

                            sync_anim.clearAnimation();
                            b_set_access_pin.setVisibility(View.VISIBLE);
                            ((RelativeLayout)findViewById(R.id.rl_validate)).setVisibility(View.VISIBLE);

                            findViewById(R.id.rl_lockscreen).setVisibility(GONE);

                            ((RelativeLayout)findViewById(R.id.rl_authenticated_running)).setVisibility(View.GONE);
                            input_access_pin.setVisibility(View.VISIBLE);

                        }

                    }


                    ((TextView) findViewById(R.id.creator_device)).setText(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

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


        if(sp_lockscreen_enabled) ((CheckBox)findViewById(R.id.cb_lockscreen)).setChecked(true);
        else ((CheckBox)findViewById(R.id.cb_lockscreen)).setChecked(false);

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
                            startActivity(new Intent(RunningActivity.this, QRActivity.class));

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

    // Handle incoming share intent
    private void handleSendText(Intent intent) {
        // Get the text from intent
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        // When Text is not null
        if (sharedText != null) {
            // Show the text as Toast message
            Toast.makeText(this, sharedText, Toast.LENGTH_LONG).show();
        }
    }
}

