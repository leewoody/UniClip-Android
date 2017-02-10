package com.piyushagade.uniclip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class AutoStart extends BroadcastReceiver
{
    public void onReceive(Context ctx, Intent arg1)
    {
        Intent intent = new Intent(ctx, UniClipService.class);
        intent.putExtra("isAutorun", "true");
        ctx.startService(intent);
    }

    //Make toast method
    private void makeToast(Context ctx, Object data) {
        Toast.makeText(ctx,String.valueOf(data),Toast.LENGTH_LONG).show();
    }
}