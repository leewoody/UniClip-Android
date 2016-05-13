package com.piyushagade.uniclip;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.firebase.client.Firebase;

public class QRActivity extends Activity implements QRCodeReaderView.OnQRCodeReadListener {

    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    private TextView myTextView;
    private QRCodeReaderView mydecoderview;

    private Firebase fb;
    private String sp_user_email;
    SharedPreferences sp;
    private SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_decoder);

        Firebase.setAndroidContext(this);

        //Hint
        makeSnack("Scan QR code on your desktop");

        //Read SharedPreferences
        sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();

        sp_user_email = sp.getString("user_email", "unknown");


        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        mydecoderview.setOnQRCodeReadListener(this);

        myTextView = (TextView) findViewById(R.id.exampleTextView);

        myTextView.setText("");
    }


    int k = 0;

    @Override
    public void onQRCodeRead(String decoded_text, PointF[] points) {
        if(isNetworkAvailable())

            //If decoded text is a valid UniClip device ID
            if(!decoded_text.equals("") && decoded_text.length() == 18){
                if(k == 0) {
                    k = 1;

                    //Format email address (remove the .)
                    String user_node = sp_user_email.replaceAll("\\.", "");

                    //Firebase
                    fb = new Firebase("https://uniclip.firebaseio.com/cloudboard/" + user_node);

                    //Reset reauthorization state
                    fb.child("reauthorization").setValue("0");

                    //Format email address (remove the .)
                    String user_email = sp_user_email.replaceAll("\\.", "");

                    //Firebase
                    fb = new Firebase("https://uniclip.firebaseio.com/desktops/");

                    //Associate the desktop with this account
                    fb.child(decoded_text).setValue(String.valueOf(user_email));

                    makeSnack("Pairing successful.");

                    vibrate(100);

                    //Delay  tear down of the activity
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1800);
                    handler = null;

                }
            }

            else if(!decoded_text.equals("") && decoded_text.length() != 18){
                makeSnack("Invalid QR code");
            }

            else {
                makeSnack("Internet not available.");
            }

    }


    // Called when your device have no camera
    @Override
    public void cameraNotFound() {

    }

    // Called when there's no QR codes in the camera preview image
    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mydecoderview != null) mydecoderview.getCameraManager().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mydecoderview != null) mydecoderview.getCameraManager().stopPreview();
    }

    //Vibrate method
    private void vibrate(int time){
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(time);
    }

    //Make Toast
    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
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
        View v = findViewById(R.id.rl_decoder);
        Snackbar.make(v, t, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

}