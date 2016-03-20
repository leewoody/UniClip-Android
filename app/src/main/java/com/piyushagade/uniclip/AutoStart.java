package com.piyushagade.uniclip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AutoStart extends BroadcastReceiver
{
    public void onReceive(Context ctx, Intent arg1)
    {
        Intent intent = new Intent(ctx,UniClipService.class);
        intent.putExtra("isAutorun", "true");
        ctx.startService(intent);
    }
}