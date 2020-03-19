package com.ashish2117.user.streammedia.broadcastrec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ashish2117.user.streammedia.service.MyMessagingService;

public class FirebaseMessagingStarterBroadcastRec extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentt = new Intent(context, MyMessagingService.class);
        context.startService(intent);
    }
}
