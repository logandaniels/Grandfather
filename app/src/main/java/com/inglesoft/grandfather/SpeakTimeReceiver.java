package com.inglesoft.grandfather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class SpeakTimeReceiver extends WakefulBroadcastReceiver {
    public static final String TAG = "SpeakTimeReceiver";

    public SpeakTimeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent");

        Bundle bundle = new Bundle();
        bundle.putAll(intent.getExtras());

        // Start the TTS Service
        Intent startServiceIntent = new Intent(Intent.ACTION_RUN);
        startServiceIntent.setClass(context, TtsService.class)
                .putExtra(TtsService.EXTRA_BUNDLE, bundle);

        startWakefulService(context, startServiceIntent);
    }
}
