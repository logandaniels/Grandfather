package com.inglesoft.grandfather;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

    int mInterval;
    int mDuration;

    EditText mIntervalTextBox;
    EditText mDurationTextBox;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // private static final String ARG_PARAM1 = "param1";
    // private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //    private String mParam1;
    //    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p/>
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     *
     * @return A new instance of fragment TalkingClockFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TalkingClockFragment newInstance() {
        TalkingClockFragment fragment = new TalkingClockFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TalkingClockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
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
                    mInterval = Integer.valueOf(s.toString());
                } else {
                    mInterval = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        String defaultDuration = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsFragment.PREF_KEY_TTS_DURATION, "15");

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
        mDurationTextBox.setText(defaultDuration);

        Button startButton = (Button) v.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTTS();
            }
        });
        Button clearButton = (Button) v.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntervalTextBox.setText("");
                mDurationTextBox.setText("");
                mInterval = 0;
                stopTTS();
            }
        });
        return v;
    }

    public void stopTTS() {
        // Create a pendingIntent to pass to AlarmManager's cancel();
        // the intent it contains must be identical to the intents created
        // by startTTS and by TtsService's scheduleNext()

        Intent startTimerIntent = new Intent(TtsService.ACTION_SPEAK);
        startTimerIntent.setClass(getActivity(), SpeakTimeReceiver.class);
        PendingIntent cancelIntent = PendingIntent
                .getBroadcast(getActivity(), PendingIntent.FLAG_UPDATE_CURRENT, startTimerIntent, 0);

        Log.d(TAG, "Canceling any current speakTime alarms");

        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        am.cancel(cancelIntent);
    }

    private void startTTS() {
        // Build a pendingIntent to start the speech service
        float volume = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(SettingsFragment.PREF_KEY_TTS_VOLUME, ".75"));

        Intent startTimerIntent = new Intent(TtsService.ACTION_SPEAK);
        startTimerIntent.setClass(getActivity(), SpeakTimeReceiver.class);
        startTimerIntent.putExtra(TtsService.EXTRA_INTERVAL, mInterval)
                .putExtra(TtsService.EXTRA_DURATION, mDuration)
                .putExtra(TtsService.EXTRA_VOLUME, volume);

        PendingIntent broadcastIntent = PendingIntent
                .getBroadcast(getActivity(), 0, startTimerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule it to go off the first time; the service will be responsible for
        // rescheduling itself until the duration has been met
        Log.d(TAG, "Scheduling initial alarm every " + mInterval + " minutes for "
                + mDuration + " minutes.");
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + mInterval * 60 * 1000, broadcastIntent);

        } else {
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + mInterval * 60 * 1000, broadcastIntent);

        }
    }

    public void loadOngoingFragment() {

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
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
