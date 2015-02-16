package com.inglesoft.grandfather;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TalkingClockFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TalkingClockFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TalkingClockFragment extends Fragment {
    private static final String TAG = "TalkingClockFragment";

    int mIntervalInMillis;
    int mDuration;
    long mEndTimeInMillis;

    EditText mIntervalTextBox;
    EditText mDurationTextBox;

    private OnFragmentInteractionListener mListener;


    public static TalkingClockFragment newInstance() {
        TalkingClockFragment fragment = new TalkingClockFragment();
        return fragment;
    }

    public TalkingClockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_talking_clock, container, false);

        mIntervalTextBox = (EditText) v.findViewById(R.id.interval_text_box);
        mIntervalTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    mIntervalInMillis = Integer.valueOf(s.toString()) * 60 * 1000;
                } else {
                    mIntervalInMillis = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mDurationTextBox = (EditText) v.findViewById(R.id.duration_text_box);
        mDurationTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    mDuration = Integer.valueOf(s.toString());
                } else {
                    mDuration = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        String defaultDuration = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsFragment.PREF_KEY_TTS_DURATION, "15");
        mDurationTextBox.setText(defaultDuration);

        Button startButton = (Button) v.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartButtonPressed();
            }
        });

        Button clearButton = (Button) v.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntervalTextBox.setText("");
                mDurationTextBox.setText("");
                mIntervalInMillis = 0;
                TtsService.stopTTS(getActivity());
            }
        });

        return v;
    }

    private void onStartButtonPressed() {
        mEndTimeInMillis = mDuration * 60 * 1000 + SystemClock.elapsedRealtime();
        Log.d(TAG, "Scheduling initial alarm every " + (mIntervalInMillis / 60 / 1000) + " minutes for "
                + ((mEndTimeInMillis - SystemClock.elapsedRealtime()) / 60 / 1000) + " minutes.");
        TtsService.startTTS(getActivity(), mIntervalInMillis, mEndTimeInMillis);
        Intent i = new Intent(getActivity(), OngoingActivity.class);
        i.putExtra(OngoingFragment.EXTRA_END_TIME, mEndTimeInMillis);
        i.putExtra(OngoingFragment.EXTRA_INTERVAL, mIntervalInMillis);
        startActivity(i);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
