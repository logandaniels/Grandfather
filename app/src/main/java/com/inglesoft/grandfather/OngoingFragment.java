package com.inglesoft.grandfather;


import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class OngoingFragment extends Fragment {
    public static final String TAG = "OngoingFragment";

    public static final String EXTRA_END_TIME = "com.inglesoft.grandfather.EXTRA_END_TIME";
    public static final String EXTRA_INTERVAL = "com.inglesoft.grandfather.EXTRA_INTERVAL";

    Callbacks mCallbacks;

    long mEndTimeInMillis = 0;
    int mInterval = 0;

    TextView mSpeakingEveryText;
    TextView mTimeRemainingText;
    Button mExtendButton;
    Button mStopButton;
    private CountDownTimer mCountDownTimer;

    public interface Callbacks {
        public void onTimerFinish();
    }


    public OngoingFragment() {
        // Required empty public constructor
    }

    public static OngoingFragment getInstance(long endTime, int interval) {
        OngoingFragment f = new OngoingFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_END_TIME, endTime);
        args.putInt(EXTRA_INTERVAL, interval);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OngoingFragment.Callbacks");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        View v = inflater.inflate(R.layout.fragment_ongoing, container, false);

        mEndTimeInMillis = getArguments().getLong(EXTRA_END_TIME);
        mInterval = getArguments().getInt(TtsService.EXTRA_INTERVAL);


        mCountDownTimer = new CountDownTimer(mEndTimeInMillis - System.currentTimeMillis(), 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                String remaining = String.format("%d minutes remaining.", (int) millisUntilFinished / (60 * 1000));
                mSpeakingEveryText.setText(remaining);
                String next = String.format("%d seconds until next speech.", (((int) millisUntilFinished / 1000) % (mInterval * 60)));
                mTimeRemainingText.setText(next);

            }

            @Override
            public void onFinish() {
                Log.d(TAG, "Finished speaking.");
            }
        }.start();

        mSpeakingEveryText = (TextView) v.findViewById(R.id.speaking_every_textview);


        mTimeRemainingText = (TextView) v.findViewById(R.id.time_remaining_textview);


        mExtendButton = (Button) v.findViewById(R.id.extend_time_button);


        mStopButton = (Button) v.findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTTS();
                mCallbacks.onTimerFinish();
            }
        });


        return v;
    }

    private void stopTTS() {
        TtsService.stopTTS(getActivity());
    }

}