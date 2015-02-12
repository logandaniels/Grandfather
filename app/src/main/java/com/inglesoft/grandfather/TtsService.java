package com.inglesoft.grandfather;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TtsService extends Service implements TextToSpeech.OnInitListener {
    public static final String TAG = "TtsService";

    public static final String EXTRA_DURATION = "com.inglesoft.grandfather.EXTRA_DURATION";
    public static final String EXTRA_EVERY_MINUTES = "com.inglesoft.grandfather.EXTRA_EVERY_MINUTES";
    public static final String EXTRA_VOLUME = "com.inglesoft.grandfather.EXTRA_VOLUME";

    private static final int START_SERVICE = 100;
    private static final int NOTIFICATION_ID = 200;
    private static final int INCREASE_TIME = 300;
    private static final int STOP_SERVICE = 400;

    private Looper mLooper;
    private ServiceHandler mHandler;


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Handling message");
            switch (msg.arg2) {
                case START_SERVICE:
                    Log.d(TAG, "Starting TtsService");
                    mTimer = new Timer();
                    startTalking(mEvery_minutes, mDuration);
                    break;
                case STOP_SERVICE:
                    Log.d(TAG, "Stopping TtsService");
                    stopSelf();
                    break;
                case INCREASE_TIME:
                    if (mTimer != null) {
                        // Extend timer somehow? Perhaps should switch to AlarmManager
                    }
            }
        }
    }

    private float mVolume;
    private int mEvery_minutes;
    private int mDuration;

    private TextToSpeech mTTS;
    private Timer mTimer;
    private String mSpeakText;


    public TtsService() {
    }

    public void onCreate() {
        HandlerThread thread = new HandlerThread("TtsServiceHandler", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mLooper = thread.getLooper();
        mHandler = new ServiceHandler(mLooper);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = startId;
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_RUN:
                msg.arg2 = START_SERVICE;
                mDuration = intent.getIntExtra(EXTRA_DURATION, 15);
                mEvery_minutes = intent.getIntExtra(EXTRA_EVERY_MINUTES, 3);
                mVolume = intent.getFloatExtra(EXTRA_VOLUME, 0.75f);
                break;
            case Intent.ACTION_SHUTDOWN:
                msg.arg2 = STOP_SERVICE;
                break;
            case Intent.ACTION_EDIT:
                msg.arg2 = INCREASE_TIME;
                break;
        }
        Log.d(TAG, "Sending message");
        mHandler.sendMessage(msg);

        return Service.START_NOT_STICKY;
    }

    private void speak(String s) {
        mSpeakText = s;
        mTTS = new TextToSpeech(this, this);
    }


    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS initialized.");

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


    private void startTalking(int everyMinutes, final int duration) {
        // Create a persistent notification so that the service runs in the foreground
        Intent notificationIntent = new Intent(Intent.ACTION_RUN);
        notificationIntent.setClass(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent launchActivityIntent = PendingIntent
                .getActivity(this, PendingIntent.FLAG_UPDATE_CURRENT, notificationIntent, 0);
        PendingIntent extendIntent = PendingIntent
                .getActivity(this, PendingIntent.FLAG_UPDATE_CURRENT,
                        notificationIntent.setAction(Intent.ACTION_EDIT), 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.abc_ic_voice_search_api_mtrl_alpha)
                        .setContentTitle("Grandfather is speaking.")
                        .setContentText("" + duration + " minutes remaining")
                        .setContentIntent(launchActivityIntent)
                        .addAction(android.R.drawable.ic_menu_add, "Add 5 minutes", extendIntent);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);

        speak("I will speak every " + everyMinutes + " minutes for " + duration + " minutes.");


        // Create a timer to handle the speak() scheduling
        Log.i(TAG, "Starting timer");
        TimerTask task = new TimerTask() {

            final int task_duration_in_minutes = duration;
            final long startTime = Calendar.getInstance().getTimeInMillis();

            public void run() {
                if (startTime + task_duration_in_minutes * 60 * 1000 < System.currentTimeMillis()) {
                    this.cancel();
                } else {
                    DateFormat df = new SimpleDateFormat("h mm");
                    String time = df.format(Calendar.getInstance().getTime());
                    speak("It is now " + time);
                }
            }
        };

        // TODO make delay normal
//        int delayInMillis = everyMinutes * 60 * 1000 / 1000;
        int delayInMillis = 5000;
        mTimer.scheduleAtFixedRate(task, 5000, delayInMillis);

    }

    @Override
    public void onDestroy() {
        if (mTimer != null) mTimer.cancel();
        if (mTTS != null) mTTS.shutdown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
