package com.piyushagade.uniclip;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    Button b_start_stop;
    CheckBox cb_autostart, cb_prompt;
    SharedPreferences sp;
    SharedPreferences.Editor ed;
    private boolean sp_autostart, sp_prompt;
    ImageView clip_icon,  sync_anim;
    SeekBar sb_sensitivity, sb_numberShakes;
    TextView sensitivity_indicator, shakes_indicator, history, welcome_text;
    private int sensitivity, numberShakes;
    private int sp_sensitivity, sp_shakes;
    Animation fade_in, fade_out, rotate, blink;
    private RelativeLayout rl_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        //Elements
        b_start_stop = (Button) findViewById(R.id.b_start_stop);
        cb_autostart = (CheckBox) findViewById(R.id.cb_autostart);
        cb_prompt = (CheckBox) findViewById(R.id.cb_prompt);
        clip_icon = (ImageView) findViewById(R.id.clip_icon);
        sync_anim = (ImageView) findViewById(R.id.sync_anim);
        sb_sensitivity = (SeekBar) findViewById(R.id.sb_sensitivity);
        sb_numberShakes = (SeekBar) findViewById(R.id.sb_shakes);
        sensitivity_indicator = (TextView) findViewById(R.id.sensitivity_indicator);
        shakes_indicator = (TextView) findViewById(R.id.shakes_indicator);
        history = (TextView) findViewById(R.id.history);
        welcome_text = (TextView) findViewById(R.id.welcome_text);
        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        blink = AnimationUtils.loadAnimation(this, R.anim.blink);
        rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rl_settings = (RelativeLayout) findViewById(R.id.rl_settings);

        sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();

        initialize();

        b_start_stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!isServiceRunning(UniClipService.class)) {
                    Intent intent = new Intent(MainActivity.this, UniClipService.class);
                    intent.putExtra("isAutorun", "false");
                    startService(intent);
//                  startService(new Intent(getBaseContext(), UniClipService.class));

                    b_start_stop.setText("UniClip running!");
                    vibrate(50);
                    rl_settings.startAnimation(fade_out);
                    sync_anim.setVisibility(View.VISIBLE);
                    sync_anim.setAlpha(0.2f);


                    fade_out.setAnimationListener(new Animation.AnimationListener() {

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

                        }
                    });

                } else {
                    stopService(new Intent(getBaseContext(), UniClipService.class));
                    b_start_stop.setText("Start UniClip!");
                    rl_settings.startAnimation(fade_in);
                    rl_settings.setVisibility(View.VISIBLE);
                    sync_anim.setAlpha(0.00f);
                    welcome_text.setText("UniClip is a multi-device clipboard synchronization "+
                            "application, which makes sharing texts, links, etc easy.");

                }


            }

        });

        cb_autostart.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {
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

        cb_prompt.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {
                     @Override
                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                         if (isChecked) {
                             ed.putBoolean("prompt", true);
                         } else {
                             ed.putBoolean("prompt", false);
                         }
                         ed.commit();

                     }
                 }
                );

    }

    private void initialize() {
        if(!isNetworkAvailable())makeSnack("Network unavailable.");

        welcome_text.setText("UniClip is a multi-device clipboard synchronization "+
                "application, which makes sharing texts, links, etc easy.");

        sp_autostart = sp.getBoolean("autostart", true);
        sp_prompt = sp.getBoolean("prompt", true);

        if (isServiceRunning(UniClipService.class)) {
            b_start_stop.setText("UniClip running!");
            clip_icon.setImageResource(R.drawable.clipboard_icon);
            sync_anim.setAlpha(0.2f);
        } else {
            b_start_stop.setText("Start UniClip!");
            clip_icon.setImageResource(R.drawable.clipboard_icon_off);
            sync_anim.setAlpha(0.00f);

        }

        sync_anim.startAnimation(rotate);
//        sync_anim.startAnimation(blink);


        sp_sensitivity = sp.getInt("sensitivity", 2+1);
        sp_shakes = sp.getInt("shakes", 2);

        sb_numberShakes.setProgress(sp_shakes);
        sb_sensitivity.setProgress(sp_sensitivity);
        sensitivity_indicator.setText(String.valueOf(sp_sensitivity+1));
        shakes_indicator.setText(String.valueOf(sp_shakes));

        if(sp_autostart){
            cb_autostart.setChecked(true);
        }
        else {
            cb_autostart.setChecked(false);
        }

        if(sp_prompt){
            cb_prompt.setChecked(true);
        }
        else {
            cb_prompt.setChecked(false);
        }

        int value = sb_numberShakes.getProgress();
        shakes_indicator.setText(String.valueOf(value));

        sb_sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                sensitivity = progress;
                sensitivity_indicator.setText(String.valueOf(sensitivity + 1));
                ed.putInt("sensitivity", sensitivity);
                ed.commit();
            }
        });


        sb_numberShakes.setOnSeekBarChangeListener(
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
                        numberShakes = progress;
                        shakes_indicator.setText(String.valueOf(numberShakes));
                        ed.putInt("shakes", numberShakes);
                        ed.commit();

                    }
                }
        );



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

}
