package com.ashish2117.user.streammedia.broadcastrec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ashish2117.user.streammedia.service.NotificationService;

public class StartNotificationReciever extends BroadcastReceiver {
    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        context.startService(new Intent(context.getApplicationContext(), NotificationService.class));
    }

}
