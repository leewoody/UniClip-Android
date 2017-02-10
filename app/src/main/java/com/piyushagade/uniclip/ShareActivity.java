package com.piyushagade.uniclip;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ShareActivity extends AppCompatActivity {

    private ClipboardManager myClipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent shareIntent = new Intent(this, UniClipService.class);
        String action = shareIntent.getAction();
        String type = shareIntent.getType();



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
}
