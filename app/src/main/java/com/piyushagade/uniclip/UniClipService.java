package com.piyushagade.uniclip;

/**
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
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.tbouron.shakedetector.library.ShakeDetector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniClipService extends Service {
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private Firebase fb;
    private ClipboardManager myClipboard;
    private float sensitivity;
    private boolean dataAccepted;
    private boolean sp_notification, sp_autostart, sp_vibrate, sp_open_url, sp_are_creator, sp_authenticated;
    SharedPreferences sp;
    private SharedPreferences.Editor ed;
    boolean destroyed = false;
    private int sp_get_shakes, sp_get_sensitivity, sp_share_shakes, sp_share_sensitivity;
    private String sp_user_email, sp_device_name;
    Boolean usernode_created_now = false, authenticated = false;

    public static ArrayList<String> history_list_service;
    private Handler cloudListenerHandler, clipListenerHandler;
    private WindowManager windowManager;
    private boolean k;
    private boolean shareOff;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!destroyed) {
            // Get Data from Shared Preferences
            Context ctx = getApplicationContext();
            SharedPreferences pref = getSharedPreferences(PREF_FILE, 0);
            Firebase.setAndroidContext(this);

            //Read SharedPreferences
            sp = getSharedPreferences(PREF_FILE, 0);
            ed = sp.edit();

            sp_autostart = sp.getBoolean("autostart", true);
            sp_notification = sp.getBoolean("notification", true);
            sp_vibrate = sp.getBoolean("vibrate", true);
            sp_get_sensitivity = sp.getInt("get_sensitivity", 2);
            sp_get_shakes = sp.getInt("get_shakes", 2);
            sp_share_sensitivity = sp.getInt("share_sensitivity", 2);
            sp_share_shakes = sp.getInt("share_shakes", 2);
            sp_user_email = sp.getString("user_email", "unknown");
            sp_device_name = sp.getString("device_name", "unknown");
            sp_open_url = sp.getBoolean("open_url", true);;
            sp_are_creator = sp.getBoolean("creator", false);
            sp_authenticated = sp.getBoolean("authenticated", false);


            //ArrayList for storing History
            history_list_service = new ArrayList<String>();

            //Get Extras from Intent
            try {
                boolean isAutorun = Boolean.valueOf(intent.getStringExtra("isAutorun"));
                if (!sp_autostart && isAutorun) {
                    makeToast("UniClip: Service not running!");
                    this.stopSelf();
                }
            } catch (NullPointerException w) {
                makeToast("Service will continue running in the background.");
            }


            //Format email address (remove the .)
            String user_node = sp_user_email.replaceAll("\\.", "");

            //Firebase
            fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/" + user_node);

            //Check if user node exists
            if (!usernode_created_now)
                fb.child("key").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists() && !destroyed) {

                            //Set 4 digit access key
                            usernode_created_now = true;
                            int key = (int) new Random().nextInt(9999);
                            fb.child("key").setValue(String.valueOf(key));

                            //Set this device as creator
                            fb.child("creator").setValue(sp_device_name);
                            sp_are_creator = true;

                            //Persist the key
                            ed.putInt("access_pin", key).commit();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                    }
                });

            //Check if this device is creator
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

            //Auto authenticate Creator
            if(sp_are_creator) {
                sp_authenticated = true;
                ed.putBoolean("authenticated", true).commit();
            }


            //Register this device
            if(sp_authenticated)
                fb.child("devices").child(sp_device_name).setValue("1");

            //Clipboard Manager
            myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

            //Listen for changes in cloudboard
            if (!destroyed)
                fb.child("data").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        sp_authenticated = sp.getBoolean("authenticated", false);

                        if(sp_authenticated) {

                            if (snapshot.getValue() != null) {
                                //Add current incoming clip if not available in history.
                                if (!history_list_service.contains(snapshot.getValue().toString()) &&
                                        !snapshot.getValue().toString().equals(""))
                                    history_list_service.add(snapshot.getValue().toString());

                                //Set data accepted as false
                                dataAccepted = false;

                                //Set the clipboard with incoming text if both are not same
                                if (!String.valueOf(snapshot.getValue()).equals(getCBData()) &&
                                        !snapshot.getValue().toString().equals("")) {
                                    //Send notification
                                    displayNotification();

                                    //Vibrate
                                    vibrate(140);

                                    //Listening to save data to clipboard
                                    shareOff = true;

                                    //Begin shake detection
                                    shakeDetection(snapshot);

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                    }
                });

            //Listen for local clipboard changes
            myClipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    //Set data to firebase
                    if(!getCBData().equals("") && sp_authenticated)
                        fb.child("data").setValue(getCBData());

                    //Send notification
                    displayNotification();

                    //Begin shake detection
                    if(!shareOff)
                    shakeDetection(null);

                }
            });
        }

        return START_STICKY;


    }


    private void shakeDetection(final DataSnapshot snapshot) {

        k = true;

        ShakeDetector.create(this, new ShakeDetector.OnShakeListener() {
            @Override
            public void OnShake() {

                //If shake was for getting data into clipboard
                if (shareOff && snapshot != null) {
                    //If shakes required to copy data into clipboard
                    if (sp_get_shakes != 0 && !destroyed) {

                        //Set parameters
                        ShakeDetector.updateConfiguration((sp_get_sensitivity) / 2, sp_get_shakes);

                        //Set data accepted as true
                        dataAccepted = true;

                        //Update local clipboard
                        ClipData myClip = ClipData.newPlainText("text", String.valueOf(snapshot.getValue()));
                        if (!destroyed) myClipboard.setPrimaryClip(myClip);

                        //Start browser
                        if (isURL(snapshot.getValue().toString()))
                            startBrowser(snapshot.getValue().toString());

                    }

                }

                //If shake was for sharing
                else if (!shareOff && snapshot == null){

                    //Set parameters
                    ShakeDetector.updateConfiguration((sp_share_sensitivity) / 2, sp_share_shakes);

                    //Format email address (remove the .)
                    String user_node = sp_user_email.replaceAll("\\.", "");

                    //Start get friend_node activity and send clipboard data
                    if(k && !shareOff){
                        startActivity(new Intent(UniClipService.this, GetFriendNodeActivity.class)
                                .putExtra("data_shared", getCBData())
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        k = false;
                    }

                    //To disable vibrate in waitAndRemoveListener
                    dataAccepted = true;
                }

            }
        });


        //Turn off Listener in 4000 ms
        waitAndRemoveListener(4000);


        //If shakes not required to copy data into clipboard
        if (sp_get_shakes == 0 && !destroyed && shareOff && snapshot != null) {
            //Set data accepted as true
            dataAccepted = true;

            //Update local clipboard
            ClipData myClip = ClipData.newPlainText("text", String.valueOf(snapshot.getValue()));
            myClipboard.setPrimaryClip(myClip);

            //Start browser
            if (isURL(snapshot.getValue().toString()))
                startBrowser(snapshot.getValue().toString());

        }

    }


    //Display Notification
    protected void displayNotification(){
        if(sp_notification && shareOff) {
            Intent intent = new Intent(Intent.ACTION_DEFAULT, Uri.parse(""));
            PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

            String notification_text = "";
            if(sp_get_shakes != 0) notification_text = "Shake your device " + sp_get_shakes + " times to copy the incoming data to your clipboard.";
            else notification_text = "Clipboard updated.";

            Notification myNotification = new Notification.Builder(this)
                    .setContentTitle("UniClip!")
                    .setContentText(notification_text)
                    .setTicker("Incoming Data!")
                    .setStyle(new Notification.BigTextStyle().bigText(notification_text))
                    .setLights(Color.WHITE, 200, 100)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.close_x)
                    .build();

            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, myNotification);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notificationManager.cancel(1);
                }
            }, 10000);
        }
        else if(sp_notification && !shareOff) {
            Intent intent = new Intent(Intent.ACTION_DEFAULT, Uri.parse(""));
            PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

            String notification_text = "";
            if(sp_get_shakes != 0) notification_text = "Shake your device " + sp_share_shakes + " times to open share Clipboard dialog.";


            Notification myNotification = new Notification.Builder(this)
                    .setContentTitle("UniClip!")
                    .setContentText(notification_text)
                    .setTicker("Share Clipboard!")
                    .setStyle(new Notification.BigTextStyle().bigText(notification_text))
                    .setLights(Color.WHITE, 200, 100)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.close_x)
                    .build();

            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, myNotification);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notificationManager.cancel(1);
                }
            }, 10000);
        }
    }



    private void setChatHead() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        final ImageView chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.close_x);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        //this code is for dragging the chat head
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(chatHead, params);
    }

    //Start Browser method
    private void startBrowser(String url){
        if(sp_open_url) {
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://" + url;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    //Check if URL
    private boolean isURL(String url) {
        final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

        Pattern p = Pattern.compile(URL_REGEX);
        Matcher m = p.matcher(url);
        if(m.find()) {
            return true;
        }
        return false;
    }

    //Get Clipboard data method
    private String getCBData(){
        ClipData clipdata = myClipboard.getPrimaryClip();        //Get primary clip
        ClipData.Item item = null;
        if(clipdata != null){
            item = clipdata.getItemAt(0);                //Get 0th item from clipboard

            if(item.getText() != null) return item.getText().toString();
            else return "";
        }
        return "";
    }

    //On Destroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        ShakeDetector.destroy();
        destroyed = true;

        //Deregister Device
        fb.child("devices").child(sp_device_name).setValue("0");
    }

    //Remove Listener after a time delay
    public void waitAndRemoveListener(int time){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (!destroyed) {
                    ShakeDetector.updateConfiguration(1000, 99999999);
                    ShakeDetector.stop();

                    //Reset shareOff
                    shareOff = false;

                }

                if (!dataAccepted) vibrate(220);
            }
        }, time);
    }

    //Make toast method
    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
    }

    //Network Detector method
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Vibrate device method
    private void vibrate(int time){
        if(sp_vibrate&&!destroyed)((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
    }

}