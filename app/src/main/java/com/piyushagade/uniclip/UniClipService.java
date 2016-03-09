package com.piyushagade.uniclip; /**
 * Created by Piyush Agade on 02-07-2016.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.tbouron.shakedetector.library.ShakeDetector;

import java.util.Calendar;

public class UniClipService extends Service {
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private Firebase fb;
    private ClipboardManager myClipboard;
    private float sensitivity;
    private boolean dataAccepted;
    private boolean sp_prompt, sp_autostart;
    SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private int sp_shakes, sp_sensitivity;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get Data from Shared Preferences
        Context ctx = getApplicationContext();
        SharedPreferences pref = getSharedPreferences(PREF_FILE, 0);
        Firebase.setAndroidContext(this);

        sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();

        sp_autostart = sp.getBoolean("autostart", true);
        sp_prompt = sp.getBoolean("prompt", true);
        sp_sensitivity = sp.getInt("sensitivity", 2);
        sp_shakes = sp.getInt("shakes", 2);

//        makeToast(String.valueOf(sp_shakes));

        boolean isAutorun = Boolean.valueOf(intent.getStringExtra("isAutorun"));
        if(!sp_autostart && isAutorun) {
            makeToast("UniClip: Service not running!");
            this.stopSelf();
        }


        fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/");
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //Listen for changes in cloudboard
        fb.child("data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dataAccepted = false;
                if (!String.valueOf(snapshot.getValue()).equals(getCBData())) {
                    vibrate(200);
                    shakeDetection(snapshot);
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });

        //Listen for local clipboard changes
        //Add listener to listen clipboard changes
        myClipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                fb.child("data").setValue(getCBData());
            }
        });

        return START_STICKY;
    }

    private void shakeDetection(final DataSnapshot snapshot) {
        if(sp_shakes!=0) {
            ShakeDetector.create(this, new ShakeDetector.OnShakeListener() {
                @Override
                public void OnShake() {
                    dataAccepted = true;
                    ClipData myClip = ClipData.newPlainText("text", String.valueOf(snapshot.getValue()));
                    myClipboard.setPrimaryClip(myClip);
                }
            });
            ShakeDetector.updateConfiguration((sp_sensitivity + 1) / 2, sp_shakes);
            waitAndRemoveListener(4000);
        }
        else{
            dataAccepted = true;
            ClipData myClip = ClipData.newPlainText("text", String.valueOf(snapshot.getValue()));
            myClipboard.setPrimaryClip(myClip);
        }

    }

    private String getCBData(){
        ClipData clipdata = myClipboard.getPrimaryClip();        //Get primary clip
        ClipData.Item item = clipdata.getItemAt(0);                //Get 0th item from clipboard
        return item.getText().toString();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ShakeDetector.destroy();
    }


    public void waitAndRemoveListener(int time){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ShakeDetector.updateConfiguration(1000, 99999999);
                ShakeDetector.stop();
                if(!dataAccepted)vibrate(320);
            }
        }, time);
    }

    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void vibrate(int time){
        if(sp_prompt)((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
    }

}