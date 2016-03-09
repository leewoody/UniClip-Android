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

import java.util.Calendar;

public class UniClipService extends Service {
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private Firebase fb;
    private ClipboardManager myClipboard;

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



        fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/");
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if(isNetworkAvailable()) {
            mainLogic();
            //Listen for changes in cloudboard
            fb.child("data").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!String.valueOf(snapshot.getValue()).equals(getCBData())) {
                        vibrate(1);
                        ClipData myClip = ClipData.newPlainText("text", String.valueOf(snapshot.getValue()));
                        myClipboard.setPrimaryClip(myClip);
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
        }

        else{
            Toast.makeText(UniClipService.this, "Waiting for network.", Toast.LENGTH_SHORT).show();
        }

        return START_STICKY;
    }

    private String getCBData(){
        ClipData clipdata = myClipboard.getPrimaryClip();        //Get primary clip
        ClipData.Item item = clipdata.getItemAt(0);                //Get 0th item from clipboard
        return item.getText().toString();
    }
    private void mainLogic() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void vibrate(int i) {
        SharedPreferences alarm_pref = getSharedPreferences(PREF_FILE, 0);

        switch (i) {
            case 1: // Clipboard Received
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(10);
                wait(1000);
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(10);

            case 2:// Clipboard Sent
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(10);
                wait(1500);
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(10);
        }


    }

    public void wait(int time){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mainLogic();
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

}