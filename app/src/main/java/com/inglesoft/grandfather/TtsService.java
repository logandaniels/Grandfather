package com.inglesoft.grandfather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class TtsService extends Service implements TextToSpeech.OnInitListener {
    public static final String TAG = "TtsService";

    public static final String ACTION_SPEAK = "com.inglesoft.grandfather.TtsService.ACTION_SPEAK";

    public static final String EXTRA_END_TIME = "com.inglesoft.grandfather.EXTRA_END_TIME";
    public static final String EXTRA_INTERVAL = "com.inglesoft.grandfather.EXTRA_INTERVAL";
    public static final String EXTRA_VOLUME = "com.inglesoft.grandfather.EXTRA_VOLUME";
    public static final String EXTRA_BUNDLE = "com.inglesoft.grandfather.EXTRA_BUNDLE";

    private static final int START_SERVICE = 100;
    private static final int STOP_SERVICE = 400;

    private Looper mLooper;
    private ServiceHandler mHandler;

    private PowerManager.WakeLock mLock;

    private float mVolume;
    private int mIntervalInMillis;
    private long mEndTimeInMillis;

    private TextToSpeech mTTS;
    private String mSpeakText;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg2) {
                case START_SERVICE:
                    Log.d(TAG, "Starting TtsService");
                    speakTime();
                    scheduleNext();
                    break;
                case STOP_SERVICE:
                    stopSelf();
                    break;
            }
        }
    }

    public TtsService() {
    }

    public static void stopTTS(Context context) {
        Intent startTimerIntent = new Intent(TtsService.ACTION_SPEAK);
        startTimerIntent.setClass(context, SpeakTimeReceiver.class);
        PendingIntent cancelIntent = PendingIntent
                .getBroadcast(context, 0, startTimerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d(TAG, "Canceling any current speakTime alarms");

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(cancelIntent);
    }

    public static void startTTS(Context context, int intervalInMillis, long endTimeInMillis) {
        // Manufacture a pendingIntent to start the speech service

        float volume = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SettingsFragment.PREF_KEY_TTS_VOLUME, ".75"));

        Intent speakIntent = new Intent(ACTION_SPEAK);
        speakIntent.setClass(context, SpeakTimeReceiver.class);
        speakIntent.putExtra(EXTRA_INTERVAL, intervalInMillis)
                .putExtra(EXTRA_END_TIME, endTimeInMillis)
                .putExtra(EXTRA_VOLUME, volume);

        PendingIntent broadcastIntent = PendingIntent
                .getBroadcast(context, 0, speakIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule it to go off; the service will be responsible for rescheduling itself
        // until the duration has been met
        Log.d(TAG, "Scheduling recurring alarm: " + ((endTimeInMillis - SystemClock.elapsedRealtime()) / 60 / 1000) + " minutes remaining.");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + intervalInMillis, broadcastIntent);

        } else {
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + intervalInMillis, broadcastIntent);

        }
    }


    public void onCreate() {
        HandlerThread thread = new HandlerThread("TtsServiceHandler", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mLooper = thread.getLooper();
        mHandler = new ServiceHandler(mLooper);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLock == null) {
            Log.d(TAG, "Obtaining wakelock");
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mLock.acquire();
        }
        Message msg = mHandler.obtainMessage();
        msg.arg1 = startId;
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_RUN:
                msg.arg2 = START_SERVICE;
                Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE);
                mEndTimeInMillis = bundle.getLong(EXTRA_END_TIME, 10 * 60 * 1000 + SystemClock.elapsedRealtime());
                mIntervalInMillis = bundle.getInt(EXTRA_INTERVAL, 3);
                mVolume = bundle.getFloat(EXTRA_VOLUME, 0.75f);
                break;
            case Intent.ACTION_SHUTDOWN:
                msg.arg2 = STOP_SERVICE;
                break;
        }
        if (mEndTimeInMillis > SystemClock.elapsedRealtime()) {
            mHandler.sendMessage(msg);
        }

        return Service.START_NOT_STICKY;
    }

    private void speak(String s) {
        mSpeakText = s;
        mTTS = new TextToSpeech(this, this);
    }

    private void speakTime() {
        DateFormat df = new SimpleDateFormat("h mm", Locale.getDefault());
        String time = df.format(Calendar.getInstance().getTime());
        speak("It is now " + time);
    }


    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS initialized.");
            mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {
                    stopSelf();
                }

                @Override
                public void onError(String utteranceId) {
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle bundle = new Bundle();
                bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, mVolume);
                mTTS.speak(mSpeakText, TextToSpeech.QUEUE_ADD, bundle, "TALKING_CLOCK_INIT");
            } else {
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "TALKING_CLOCK_INIT");
                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, Float.toString(mVolume));
                mTTS.speak(mSpeakText, TextToSpeech.QUEUE_ADD, params);
            }

        } else {
            Log.e(TAG, "Unable to initialize TTS");
        }
    }

    private void scheduleNext() {
        startTTS(this, mIntervalInMillis, mEndTimeInMillis);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "TtsService destroyed");
        if (mTTS != null) mTTS.shutdown();
        if (mLock != null) mLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
