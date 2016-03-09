package com.piyushagade.uniclip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AutoStart extends BroadcastReceiver
{
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0,UniClipService.class);
        intent.putExtra("isAutorun", "true");
        arg0.startService(intent);
        Log.i("Autostart", "started");
    }
}