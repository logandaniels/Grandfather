package com.inglesoft.grandfather;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class OngoingFragment extends Fragment {
    public static final String TAG = "OngoingFragment";

    TextView mSpeakingEveryText;
    TextView mTimeRemainingText;
    Button mExtendButton;
    Button mStopButton;


    public OngoingFragment() {
        // Required empty public constructor
    }

    public static OngoingFragment getInstance() {
        return new OngoingFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ongoing, container, false);

        mSpeakingEveryText = (TextView) v.findViewById(R.id.speaking_every_textview);


        mTimeRemainingText = (TextView) v.findViewById(R.id.time_remaining_textview);


        mExtendButton = (Button) v.findViewById(R.id.extend_time_button);


        mStopButton = (Button) v.findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTTS();
            }
        });


        return v;
    }

    private void stopTTS() {
        ((TalkingClockFragment) getParentFragment()).stopTTS();
    }


}
