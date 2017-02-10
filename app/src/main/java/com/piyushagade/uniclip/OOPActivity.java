package com.piyushagade.uniclip;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Random;

public class OOPActivity extends AppCompatActivity {

    private ClipboardManager myClipboard;
    private String sp_user_email;
    private SharedPreferences sp;
    private SharedPreferences.Editor ed;
    private static final String PREF_FILE = "com.piyushagade.uniclip.preferences";
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get SharedPreferences
        sp = getSharedPreferences(PREF_FILE, 0);
        ed = sp.edit();

        sp_user_email = sp.getString("user_email", "unknown");

        //Format email address (remove the .)
        String user_node = encrypt(encrypt(encrypt(sp_user_email.replaceAll("\\.", ""))));

        //Firebase
        DatabaseReference fb = mRootRef.child("cloudboard").child(user_node);

        Uri data = this.getIntent().getData();
        if (data != null && data.isHierarchical()) {
            String uri = this.getIntent().getDataString();
            uri = uri.toLowerCase();

            int key = (int) new Random().nextInt(9999);
            if (key < 1000) key = key * 10 + new Random().nextInt(99);
            else if (key < 100) key = key * 100 + new Random().nextInt(9);

            if(sp.getBoolean("used_desktop_client", false) && sp.getInt("desktop_version_in_use", 0) >= 3)
                fb.child("link").setValue(uri+'%'+key);
            else if(sp.getInt("desktop_version_in_use", 0) < 3)
                makeToast("Please update your desktop client to the latest version.");
            else
                makeToast("No desktop client is currently in your UniClip's network. Install/run on your PC to use this feature.");
        }


        //Clipboard Manager
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (getIntent() != null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
            String receivedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            Toast.makeText(this, "Text broadcasted to your cloudboard.", Toast.LENGTH_SHORT).show();

            //Update local clipboard
            ClipData myClip = ClipData.newPlainText("text", String.valueOf(receivedText));
            myClipboard.setPrimaryClip(myClip);

        }

        finish();

    }

    //Make Toast
    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
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
