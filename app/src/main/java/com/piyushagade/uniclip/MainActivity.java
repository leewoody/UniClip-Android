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
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends Activity{
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    Button b_start_stop, b_clear_history, b_view_access_pin, b_set_access_pin, b_diagnose, b_go_back_to_main, b_manage_friends, b_help_manage_friends;
    CheckBox cb_autostart, cb_notification, cb_vibrate, cb_theme, cb_open_url;
    EditText input_access_pin;
    SharedPreferences sp;
    SharedPreferences.Editor ed;
    private boolean sp_autostart, sp_notification, sp_vibrate, sp_theme, sp_first_run, sp_open_url, sp_are_creator, sp_authenticated;
    private String sp_user_email, sp_device_name;
    ImageView clip_icon, sync_anim, b_close, b_menu, b_back, b_info, b_user, b_history, b_help, b_help_get, b_help_share;
    SeekBar sb_get_sensitivity, sb_get_numberShakes, sb_share_sensitivity, sb_share_numberShakes;
    TextView get_sensitivity_indicator, get_shakes_indicator, access_pin_desc, welcome_text;
    TextView share_sensitivity_indicator, share_shakes_indicator;
    private int get_sensitivity, get_numberShakes, share_sensitivity, share_numberShakes;
    private int sp_get_sensitivity, sp_get_shakes, sp_share_sensitivity, sp_share_shakes, sp_unread;
    Animation fade_in, fade_out, rotate, blink, slide_in_top, slide_out_top, fade_in_rl_top, fade_out_rl_top, bob, fade_out_rl_settings;
    private RelativeLayout rl_settings, rl_running, rl_main, rl_top, rl_menu_on, rl_menu_content, rl_history, rl_info, rl_user, rl_help, rl_first_page;
    private ClipboardManager myClipboard;
    private ArrayList<String> history_list_activity;
    private Handler handler_history, handler_status, handler_connection;
    private TextView user_access_pin, status_service, status_connection;
    public static int colorPrimary, colorAccent;
    private boolean usernode_created_now = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        Firebase.setAndroidContext(this);

        setContentView(R.layout.activity_main);

        //UI Components
        b_start_stop = (Button) findViewById(R.id.b_start_stop);
        b_clear_history = (Button) findViewById(R.id.b_clear_history);
        b_view_access_pin = (Button) findViewById(R.id.b_view_access_pin);
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
        user_access_pin = (TextView) findViewById(R.id.user_access_pin);
        status_connection = (TextView) findViewById(R.id.status_connection);
        status_service = (TextView) findViewById(R.id.status_service);

        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_in_rl_top = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_out_rl_top = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_out_rl_settings = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        bob = AnimationUtils.loadAnimation(this, R.anim.bob);
        blink = AnimationUtils.loadAnimation(this, R.anim.blink);
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        slide_in_top = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        slide_out_top = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);

        rl_settings = (RelativeLayout) findViewById(R.id.rl_settings);
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


        //Ripple Effect for componenets

        //Start_Stop Button
        MaterialRippleLayout.on(b_start_stop).rippleColor(getResources().getColor(R.color.colorAccent))
                .rippleAlpha(0.92f).rippleDuration(500)
                .create();

        //Menu
        MaterialRippleLayout.on(b_view_access_pin).rippleColor(Color.WHITE)
                .rippleAlpha(0.2f).rippleDuration(200).rippleRoundedCorners(140)
                .create();

        //Manage friends
        MaterialRippleLayout.on(b_manage_friends).rippleColor(Color.WHITE)
                .rippleAlpha(0.2f).rippleDuration(200).rippleRoundedCorners(140)
                .create();

        MaterialRippleLayout.on(b_help_manage_friends).rippleColor(Color.WHITE)
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

        //Diagnose Button listener
        b_diagnose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                b_diagnose.setText("Checking");
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b_diagnose.setText("Diagnose");
                        diagnose();
                    }
                }, 3000);

            }

        });

        //Manage friends Button listener
        b_manage_friends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(50);

                startActivity(new Intent(MainActivity.this, ManageFriendsActivity.class).putExtra("prev_screen", "main_screen_menu"));
            }

        });

        //Manage friends from Help menu Button listener
        b_help_manage_friends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(50);

                startActivity(new Intent(MainActivity.this, ManageFriendsActivity.class).putExtra("prev_screen", "main_screen_menu"));
            }

        });

        //Close Button listener
        b_go_back_to_main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                b_start_stop.setVisibility(View.VISIBLE);
                ed.putBoolean("authenticated", false).commit();

                b_start_stop.setText("Start UniClip!");
                rl_settings.startAnimation(fade_in);
                rl_settings.setVisibility(View.VISIBLE);

                rl_running.startAnimation(fade_out);
                rl_running.setVisibility(View.GONE);


                //Animate app title
                swingAnimate(findViewById(R.id.app_title), 700, 1000);

                sync_anim.setAlpha(0.00f);
                welcome_text.setText("UniClip is a multi-device clipboard synchronization " +
                        "application, which makes sharing texts, links, etc easy.");

                rl_menu_content.setVisibility(View.GONE);

                b_go_back_to_main.setVisibility(View.GONE);
            }
        });

        //Service start stop listener
        b_start_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!isServiceRunning(UniClipService.class)) {

                    Intent intent = new Intent(MainActivity.this, UniClipService.class);
                    intent.putExtra("isAutorun", "false");
                    startService(intent);

                    //Reset Button text for main screen
                    b_start_stop.setText("Start UniClip!");

                    //Reinitialize
                    reInitialize();

                    //Animate app title
                    swingAnimate(findViewById(R.id.app_title), 700, 1000);

                    rl_menu_content.setVisibility(View.GONE);

                    b_start_stop.setText("Stop UniClip!");
                    vibrate(50);

                    //Animations
                    rl_settings.startAnimation(fade_out_rl_settings);
                    fade_out_rl_settings.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            rl_settings.setVisibility(View.GONE);
                            welcome_text.setText("You can close this app. The background service will make " +
                                    "sure clipboards on all your devices stay unified.");

                            rl_running.setVisibility(View.VISIBLE);
                            rl_running.startAnimation(fade_in);
                            b_set_access_pin.setEnabled(true);
                            b_set_access_pin.setText("Validate");

                            sync_anim.setVisibility(View.VISIBLE);
                            sync_anim.setAlpha(0.10f);


                            clip_icon.startAnimation(bob);

                        }
                    });


                    //Set Status in Running Screen
                    handler_status = new Handler();
                    refreshServiceStatus.run();

                    handler_connection = new Handler();
                    refreshConnectionStatus.run();

                }

                else if (!isServiceRunning(UniClipService.class) && !sp_are_creator) {

                    makeToast("Here 2");

                    //Reinitialize
                    reInitialize();

                    //Animate app title
                    swingAnimate(findViewById(R.id.app_title), 700, 1000);

                    rl_menu_content.setVisibility(View.GONE);
                    b_go_back_to_main.setVisibility(View.VISIBLE);

                    b_start_stop.setText("Stop UniClip!");
                    b_start_stop.setVisibility(View.GONE);
                    vibrate(50);

                    //Animations
                    rl_settings.startAnimation(fade_out_rl_settings);
                    fade_out_rl_settings.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            rl_settings.setVisibility(View.GONE);
                            welcome_text.setText("You can close this app. The background service will make " +
                                    "sure clipboards on all your devices stay unified.");

                            rl_running.setVisibility(View.VISIBLE);
                            rl_running.startAnimation(fade_in);
                            b_set_access_pin.setEnabled(true);
                            b_set_access_pin.setText("Validate");

                            sync_anim.setVisibility(View.VISIBLE);
                            sync_anim.setAlpha(0.10f);


                            clip_icon.startAnimation(bob);

                        }
                    });


                    //Set Status in Authentication Screen
                    handler_status = new Handler();
                    refreshServiceStatus.run();

                    handler_connection = new Handler();
                    refreshConnectionStatus.run();

                }

                //Service was running
                else {
                    stopService(new Intent(getBaseContext(), UniClipService.class));
                    b_start_stop.setVisibility(View.VISIBLE);
                    ed.putBoolean("authenticated", false).commit();

                    b_start_stop.setText("Start UniClip!");
                    rl_settings.startAnimation(fade_in);
                    rl_settings.setVisibility(View.VISIBLE);

                    rl_running.startAnimation(fade_out);
                    rl_running.setVisibility(View.GONE);


                    //Animate app title
                    swingAnimate(findViewById(R.id.app_title), 700, 1000);

                    sync_anim.setAlpha(0.00f);
                    welcome_text.setText("UniClip is a multi-device clipboard synchronization " +
                            "application, which makes sharing texts, links, etc easy.");

                    rl_menu_content.setVisibility(View.GONE);

                }

            }

        });

        //Access Pin Button listener
        b_view_access_pin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                getAccessPin();
                int key = sp.getInt("access_pin", 0);
                if(key != 0){
                    b_view_access_pin.setText("Access PIN: " + String.valueOf(key));
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            b_view_access_pin.setText("View Access Pin");
                        }
                    }, 10000);
                }
                else {
                    b_view_access_pin.setText("Error");
                }


            }

        });


        //Menu button listener
        b_menu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(26);

                //MenuIntroActivity
                menuShowcaseInitiate();

                rl_first_page.setVisibility(View.VISIBLE);
                rl_user.setVisibility(View.INVISIBLE);
                rl_info.setVisibility(View.INVISIBLE);
                rl_history.setVisibility(View.INVISIBLE);
                rl_help.setVisibility(View.INVISIBLE);


                rl_top.startAnimation(fade_out_rl_top);
                rl_menu_on.setVisibility(View.VISIBLE);
                rl_menu_on.startAnimation(fade_in_rl_top);

                fade_in_rl_top.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        rl_top.setVisibility(View.INVISIBLE);
                        rl_menu_content.setVisibility(View.VISIBLE);
                        rl_menu_content.startAnimation(slide_in_top);
                    }
                });

            }


        });

        //Back button listener
        b_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                //Animate app title
                swingAnimate(findViewById(R.id.app_title), 700, 1500);

                rl_menu_on.startAnimation(fade_out_rl_top);
                rl_top.setVisibility(View.VISIBLE);
                rl_top.startAnimation(fade_in_rl_top);

                //Wait for rl_top to reappear
                fade_in_rl_top.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        rl_menu_on.setVisibility(View.INVISIBLE);
                        rl_menu_content.startAnimation(slide_out_top);

                        //Wait for rl_menu_content to slide up
                        slide_out_top.setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                rl_menu_content.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });
            }
        });

        //History button listener
        b_history.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                //Reset Shortcut badger
                ed.putInt("unread", 0).commit();
                updateBadger();

                rl_first_page.setVisibility(View.INVISIBLE);
                rl_history.setVisibility(View.VISIBLE);
                rl_info.setVisibility(View.INVISIBLE);
                rl_user.setVisibility(View.INVISIBLE);
                rl_help.setVisibility(View.INVISIBLE);

                handler_history = new Handler();
                handler_history.postDelayed(refreshHistory, 0);
            }
        });

        //Help for get button listener
        b_help_get.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                makeToast("See 'Sharing Clipboard, section 1.");

                //Open help menu
                rl_first_page.setVisibility(View.INVISIBLE);
                rl_menu_on.setVisibility(View.VISIBLE);
                rl_menu_content.setVisibility(View.VISIBLE);
                rl_first_page.setVisibility(View.INVISIBLE);
                rl_help.setVisibility(View.VISIBLE);
                rl_user.setVisibility(View.INVISIBLE);
                rl_history.setVisibility(View.INVISIBLE);
                rl_info.setVisibility(View.INVISIBLE);
            }
        });

        //Help for share button listener
        b_help_share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                makeToast("See 'Sharing Clipboard, section 2.");

                //Open help menu
                rl_first_page.setVisibility(View.INVISIBLE);
                rl_menu_on.setVisibility(View.VISIBLE);
                rl_menu_content.setVisibility(View.VISIBLE);
                rl_help.setVisibility(View.VISIBLE);
                rl_user.setVisibility(View.INVISIBLE);
                rl_history.setVisibility(View.INVISIBLE);
                rl_info.setVisibility(View.INVISIBLE);

            }
        });

        //Validate access pin button listener
        b_set_access_pin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                if(!isNetworkAvailable()){
                    makeSnack("Network unavailable");
                }
                else {
                    b_set_access_pin.setText("Checking");
                }

                final String input_pin = input_access_pin.getText().toString();

                //Format email address (remove the .)
                String user_node = sp_user_email.replaceAll("\\.", "");

                //Firebase
                Firebase fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/" + user_node);

                fb.child("key").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            //Correct Access Pin
                            if (input_pin.equals(snapshot.getValue().toString())) {
                                input_access_pin.setVisibility(View.GONE);
                                b_set_access_pin.setText("Verified");
                                b_set_access_pin.setEnabled(false);

                                b_start_stop.setVisibility(View.VISIBLE);

                                b_go_back_to_main.setVisibility(View.GONE);

                                sync_anim.startAnimation(rotate);
                                ed.putBoolean("authenticated", true).commit();

                                //Restart Service
                                stopService(new Intent(getBaseContext(), UniClipService.class));
                                Intent intent = new Intent(MainActivity.this, UniClipService.class);
                                intent.putExtra("isAutorun", "false");
                                startService(intent);

                            }
                            else {
                                makeToast("Wrong Access Pin. Try Again");

                                //Animate input on wrong password
                                swingAnimate(findViewById(R.id.input_access_pin), 600, 300);

                                ed.putBoolean("authenticated", false).commit();
                                b_set_access_pin.setText("Validate");

                            }

                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                    }
                });

            }
        });

        //User button listener
        b_user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);

                rl_first_page.setVisibility(View.INVISIBLE);
                rl_user.setVisibility(View.VISIBLE);
                rl_info.setVisibility(View.INVISIBLE);
                rl_history.setVisibility(View.INVISIBLE);
                rl_help.setVisibility(View.INVISIBLE);

                //Set data
                TextView tv_username  = (TextView) findViewById(R.id.user_username);
                TextView tv_device  = (TextView) findViewById(R.id.user_device);
                tv_username.setText(sp_user_email);
                tv_device.setText(sp_device_name);

                getAccessPin();
                int key = sp.getInt("access_pin", 0);
                if(sp_are_creator)user_access_pin.setText(String.valueOf(key));

                handler_history = new Handler();
                handler_history.postDelayed(refreshDevicesList, 0);
            }
        });

        //Info button listener
        b_info.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);


                rl_first_page.setVisibility(View.INVISIBLE);
                rl_info.setVisibility(View.VISIBLE);
                rl_user.setVisibility(View.INVISIBLE);
                rl_history.setVisibility(View.INVISIBLE);
                rl_help.setVisibility(View.INVISIBLE);
            }
        });


        //Help button listener
        b_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);


                rl_first_page.setVisibility(View.INVISIBLE);
                rl_help.setVisibility(View.VISIBLE);
                rl_user.setVisibility(View.INVISIBLE);
                rl_history.setVisibility(View.INVISIBLE);
                rl_info.setVisibility(View.INVISIBLE);
            }
        });

        //Clear history listener
        b_clear_history.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(27);
                history_list_activity.clear();
                UniClipService.history_list_service.clear();
                setHistoryListItems();
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


    private Runnable refreshServiceStatus = new Runnable() {
        @Override
        public void run() {
            sp_authenticated = sp.getBoolean("authenticated", false);

            if(isServiceRunning(UniClipService.class) && sp_authenticated){

                if(isNetworkAvailable())status_service.setText("Service:\n  Running. Listening to the cloudboard.");
                else status_service.setText("Service:\n  Running. Waiting for network.");
            }

            else if(isServiceRunning(UniClipService.class) && !sp_authenticated){

                //Waiting for authentication
                if(isNetworkAvailable())status_service.setText("Service:\n  Waiting for authentication.");
                else {
                    status_service.setText("Service:\n  Not running. Waiting for network.");
                }
            }

            //If service not running
            else{
                status_service.setText("Service:\n  Error. Restart the application.");
            }

            handler_status.postDelayed(this, 6000);
        }
    };

    private Runnable refreshConnectionStatus = new Runnable() {
        @Override
        public void run() {
            if(isNetworkAvailable()){
                status_connection.setText("Connection:\n  Connected to the server.");
                b_diagnose.setVisibility(View.GONE);
            }
            else {
                status_connection.setText("Connection:\n  Internet unavailable.");
                b_diagnose.setVisibility(View.VISIBLE);
            }

            handler_connection.postDelayed(this, 6000);
        }
    };

    //Get Access Pin
    private void getAccessPin() {
        //Format email address (remove the .)
        String user_node = sp.getString("user_email", "").replaceAll("\\.", "");

        //Firebase
        Firebase fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/" + user_node);

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
            public void onCancelled(FirebaseError error) {
            }
        });
        fb = null;
    }

    //Reinitialize
    private void reInitialize() {

        //Get Values SP
        sp_are_creator = sp.getBoolean("creator", false);

        //Go back button set Gone
        b_go_back_to_main.setVisibility(View.GONE);

        //Reset authentication
        ed.putBoolean("authenticated", false).commit();
        sp_authenticated = sp.getBoolean("authenticated", false);

        //Get user email
        if(hasPermission()) {
            AccountManager accountManager = AccountManager.get(MainActivity.this);
            Account account = getAccount(accountManager);

            if (account != null) {
                ed.putString("user_email", account.name);
                ed.commit();
            } else
                makeSnackForPermissions("Grant 'Contacts' permission to UniClip!");
        }


        //Set content if this device is/is not the creator
        final Handler creatorHandler = new Handler();
        creatorHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sp_are_creator = sp.getBoolean("creator", false);
                if(!sp_are_creator){
                    access_pin_desc.setText("In order to start listening to the cloudboard, you need to input the Access Pin. You can find Access Pin in the Menu of the device that created the cloudboard.");

                    b_view_access_pin.setVisibility(View.GONE);

                    sync_anim.clearAnimation();
                    b_set_access_pin.setVisibility(View.VISIBLE);
                    input_access_pin.setVisibility(View.VISIBLE);
                }
                else {
                    //Reset Access Pin Button text
                    b_view_access_pin.setText("View Access Pin");
                    b_view_access_pin.setVisibility(View.VISIBLE);

                    sync_anim.startAnimation(rotate);
                    b_set_access_pin.setVisibility(View.INVISIBLE); //Let this be invisible
                    input_access_pin.setVisibility(View.GONE);
                }
            }
        }, 600);


        //Format email address (remove the .)
        String user_node = sp_user_email.replaceAll("\\.", "");

        //Firebase
        Firebase fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/" + user_node);

        //Check if this device is creator
        fb.child("creator").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    if(sp_device_name.equals(snapshot.getValue().toString())){
                        ed.putBoolean("creator", true).commit();
                    }
                    else ed.putBoolean("creator", false).commit();
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    //History refresh with 4 sec delay
    private Runnable refreshHistory = new Runnable() {
        public void run() {
            setHistoryListItems();

            handler_history.postDelayed(this, 4000);
        }
    };


    //Refresh device list runnable with 4 sec delay
    private Runnable refreshDevicesList = new Runnable() {
        public void run() {
            setRegisteredDeviceList();
            handler_history.postDelayed(this, 4000);
        }
    };

    //Check if a number is even
    private boolean isEven(int i) {
        if(i % 2 == 0) return true;
        return false;
    }

    //Set devices in feed
    private void setRegisteredDeviceList() {
        int i = 1;
        final LinearLayout ll_other_devices_feed = (LinearLayout) findViewById(R.id.ll_other_devices_feed);
        ll_other_devices_feed.removeAllViews();

        final Firebase fb_devices = new Firebase("https://uniclip.firebaseio.com/cloudboard/" +
                sp_user_email.replaceAll("\\.", "") + "/devices");

        fb_devices.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ll_other_devices_feed.removeAllViews();

                int i = 1;    //Serial number
                if (snapshot.getChildrenCount() != 0)
                    for (final DataSnapshot postSnapshot : snapshot.getChildren()) {

                        final TextView row1 = new TextView(getBaseContext());
                        row1.generateViewId();
                        row1.setPadding(20, 12, 20, 12);

                        row1.setText(i + ". " + postSnapshot.getKey().toString());
                        i++;

                        if(postSnapshot.getValue().toString().equals("1"))row1.setTextColor(Color.parseColor("#AAFFFFFF"));
                        else if(postSnapshot.getValue().toString().equals("0"))row1.setTextColor(Color.parseColor("#AC2358"));
                        row1.setTextSize(16);

                        row1.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if(postSnapshot.getValue().toString().equals("1"))makeToast(postSnapshot.getKey().toString() + " is listening.");
                                else if(postSnapshot.getValue().toString().equals("0"))makeToast(postSnapshot.getKey().toString() + " is inactive.");
                            }

                        });

                        ll_other_devices_feed.addView(row1);
                        ll_other_devices_feed.setVisibility(View.VISIBLE);
                    }
                else {
                    final TextView row1 = new TextView(getBaseContext());
                    row1.generateViewId();
                    row1.setPadding(20, 12, 20, 12);
                    row1.setText("No registered devices.");

                    row1.setTextColor(Color.parseColor("#AAFFFFFF"));
                    row1.setTypeface(Typeface.MONOSPACE);
                    row1.setTextSize(16);

                    ll_other_devices_feed.addView(row1);
                    ll_other_devices_feed.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
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
                    row1.generateViewId();
                    row1.setPadding(20, 12, 30, 12);

                    row1.setText(i + ". " + listItem.toString());
                    i++;

                    if(isEven(i))row1.setTextColor(Color.parseColor("#CCFFFFFF"));
                    else row1.setTextColor(Color.parseColor("#88FFFFFF"));

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
                row1.generateViewId();
                row1.setPadding(20, 12, 20, 12);
                row1.setText("Clipboard history is empty!");

                row1.setTextColor(Color.parseColor("#88FFFFFF"));
                row1.setTextSize(16);

                ll_history_feed.addView(row1);
                ll_history_feed.setVisibility(View.VISIBLE);

            }
    }


    //Initialize application
    private void initialize() {

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
            finish();
        }

        //Make snack if network unavailable
        //if(!isNetworkAvailable())makeSnack("Network unavailable.");

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
        if(hasPermission()) {
            AccountManager accountManager = AccountManager.get(MainActivity.this);
            Account account = getAccount(accountManager);

            if (account != null) {
                ed.putString("user_email", account.name);
                ed.commit();
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

        rl_menu_content.setVisibility(View.INVISIBLE);


        //Format email address (remove the .)
        String user_node = sp_user_email.replaceAll("\\.", "");

        //Firebase
        Firebase fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/" + user_node);

        //Get access pin
        getAccessPin();

        //Check if this device is creator
        fb.child("creator").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    if(sp_device_name.equals(snapshot.getValue().toString())){
                        ed.putBoolean("creator", true).commit();
                    }
                    else ed.putBoolean("creator", false).commit();
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });


        //Set up content if service is running
        if (isServiceRunning(UniClipService.class)) {

            //Set Status in Authentication Screen
            handler_status = new Handler();
            refreshServiceStatus.run();

            handler_connection = new Handler();
            refreshConnectionStatus.run();


            b_start_stop.setText("Stop UniClip!");

            rl_settings.setVisibility(View.GONE);
            rl_running.setVisibility(View.VISIBLE);
            sync_anim.setAlpha(0.1f);

            welcome_text.setText("You can close this app. The background service will make " +
                    "sure clipboards on all your devices stay unified.");


            //Set content if creator
            if(sp_are_creator) {
                if (sp_authenticated) {

                    if (isNetworkAvailable())
                        status_service.setText("Service:\n  Running. Listening to the cloudboard.");
                    else status_service.setText("Service:\n  Running. Waiting for network.");
                } else if (!sp_authenticated) {

                    //Waiting for authentication
                    if (isNetworkAvailable())
                        status_service.setText("Service:\n  Waiting for authentication.");
                    else {
                        status_service.setText("Service:\n  Not running. Waiting for network.");
                    }
                }
            }
            //Set content if not creator
            else {
                if (sp_authenticated) {
                    b_set_access_pin.setVisibility(View.VISIBLE);
                    b_set_access_pin.setText("Verified");
                    b_view_access_pin.setVisibility(View.GONE);
                    b_set_access_pin.setEnabled(false);
                    input_access_pin.setVisibility(View.GONE);

                    if (isNetworkAvailable()) {
                        status_service.setText("Service:\n  Running. Listening to the cloudboard.");
                        status_connection.setText("Connection:\n  Connected to the server.");
                    }
                    else{
                        status_service.setText("Service:\n  Running. Waiting for network.");
                        status_connection.setText("Connection:\n  Internet unavailable.");
                    }
                }
                else if (!sp_authenticated) {
                    access_pin_desc.setText("In order to start listening to the cloudboard, you need to input the Access Pin. You can find Access Pin in the Menu of the device that created the cloudboard.");
                    b_view_access_pin.setVisibility(View.GONE);

                    sync_anim.clearAnimation();
                    b_set_access_pin.setVisibility(View.VISIBLE);
                    input_access_pin.setVisibility(View.VISIBLE);

                    //Waiting for authentication
                    if (isNetworkAvailable()){
                        status_service.setText("Service:\n  Waiting for authentication.");
                        status_connection.setText("Connection:\n  Connected to the server.");
                    }
                    else {
                        status_service.setText("Service:\n  Can't authenticate. No network.");
                        status_connection.setText("Connection:\n  Internet unavailable.");
                    }
                }
            }

        }


        //Service not running
        else {
            b_start_stop.setText("Start UniClip!");
            rl_settings.setVisibility(View.VISIBLE);
            rl_running.setVisibility(View.GONE);
            sync_anim.setAlpha(0.00f);

            welcome_text.setText("UniClip is a multi-device clipboard synchronization " +
                    "application, which makes sharing texts, links, etc easy.");
        }


        //Sync Icon Animation
        sync_anim.startAnimation(rotate);


        //Set Shake Sensitivity and number of Shakes
        sp_get_sensitivity = sp.getInt("get_sensitivity", 2+1);
        sp_get_shakes = sp.getInt("get_shakes", 2);
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

    //Check if user exists, if not set this as creator
    private void checkIfCreator(){

        //Format email address (remove the .)
        String user_node = sp_user_email.replaceAll("\\.", "");

        //Firebase
        final Firebase fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/" + user_node);

        //Check if user node exists
        if (!usernode_created_now && fb != null)
            fb.child("key").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {

                        //Set 4 digit access key
                        usernode_created_now = true;
                        int key = (int) new Random().nextInt(9999);
                        fb.child("key").setValue(String.valueOf(key));

                        //Set this device as creator
                        fb.child("creator").setValue(sp_device_name);

                        //Persist the key
                        ed.putInt("access_pin", key).commit();
                    }
                }

                @Override
                public void onCancelled(FirebaseError error) {
                }
            });

        //Check if this device is creator
        if(fb != null)
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
                    }
                }

                @Override
                public void onCancelled(FirebaseError error) {
                }
            });

        if(sp.getBoolean("creator", false))
            sp_authenticated = true;


        //Register this device
        if(sp_authenticated )
            fb.child("devices").child(sp_device_name).setValue("1");
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

    //Menu showcase
    private void menuShowcaseInitiate() {
        //Menu Button
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .setMaskColor(Color.parseColor("#00ffc107"))
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.NORMAL)
                .setDelayMillis(100)
                .enableFadeAnimation(true)
                .performClick(true)
                .dismissOnTouch(false)
                .setInfoText("Clipboard History: This section shows the clipboard history. Clicking on an item will copy it to the clipboard." +
                        "\n\nClick on the history icon to continue.")
                .setTarget(b_history)
                .setUsageId("card_history")
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String materialIntroViewId) {
                        //User page
                        new MaterialIntroView.Builder(MainActivity.this)
                                .enableDotAnimation(true)
                                .setMaskColor(Color.parseColor("#00ffc107"))
                                .setFocusGravity(FocusGravity.CENTER)
                                .setFocusType(Focus.NORMAL)
                                .setDelayMillis(100)
                                .enableFadeAnimation(true)
                                .performClick(true)
                                .dismissOnTouch(false)
                                .setInfoText("User Info: This section has your username. device name, access pin, and list of registered devices.")
                                .setTarget(b_user)
                                .setUsageId("card_user")
                                .setListener(new MaterialIntroListener() {
                                    @Override
                                    public void onUserClicked(String materialIntroViewId) {
                                        //Back Button
                                        new MaterialIntroView.Builder(MainActivity.this)
                                                .enableDotAnimation(true)
                                                .setMaskColor(Color.parseColor("#00ffc107"))
                                                .setFocusGravity(FocusGravity.CENTER)
                                                .setFocusType(Focus.MINIMUM)
                                                .setDelayMillis(100)
                                                .enableFadeAnimation(true)
                                                .performClick(true)
                                                .dismissOnTouch(false)
                                                .setInfoText("Click here to close the menu.")
                                                .setTarget(b_back)
                                                .setUsageId("card_back")
                                                .show();
                                    }
                                })
                                .show();
                    }
                })
                .show();


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
                                .setTarget(b_start_stop)
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
                                                .setTarget(rl_settings)
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

    //Check if 'Contact' permission is granted
    private boolean hasPermission()
    {
        String permission = "android.permission.GET_ACCOUNTS";
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    //Diagnose Method
    private void diagnose(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        //3G check
        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        //WiFi Check
        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();

        if (!is3g && !isWifi){
            makeToast("Both Wifi and Mobile data are off.");
        }
        else if (is3g && !isWifi){
            makeToast("Mobile data is on, Wifi is off. Check if you are in network range.");
        }
        else if (!is3g && isWifi){
            makeToast("There seems to be a problem with Wifi. Try Mobile data instead.");
        }
    }

    private void updateBadger() {
        sp_unread = sp.getInt("unread", 0);
        //ShortcutBadger
        ShortcutBadger.applyCount(getApplication(), sp_unread);
    }

}
