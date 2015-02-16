package com.inglesoft.grandfather;


import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;


public class OngoingActivity extends BaseActivity
        implements OngoingFragment.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing);

        Bundle extras = getIntent().getExtras();
        long endTime = extras.getLong(OngoingFragment.EXTRA_END_TIME);
        int interval = extras.getInt(OngoingFragment.EXTRA_INTERVAL);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.ongoing_fragment_container, OngoingFragment.getInstance(endTime, interval)).commit();
    }


    @Override
    public void onTimerFinish() {
        finish();
    }

}
