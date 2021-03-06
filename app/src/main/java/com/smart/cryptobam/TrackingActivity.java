package com.smart.cryptobam;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.smart.cryptobam.tracking.Tracker;

public class TrackingActivity extends AppCompatActivity {
    protected Tracker mTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTracker = new Tracker(this);
    }
}
