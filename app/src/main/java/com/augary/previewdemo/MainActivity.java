package com.augary.previewdemo;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.augary.app.*;
import com.augary.augaryforpedigree.R;


public class MainActivity extends Activity implements View.OnClickListener{


    private Button btnPreview;
    private String device_id;
    private Button btnTrigger;
    private Button btnReboot;
    private Button btnSetup;
    private SeekBar seekBar;

    private boolean previewing = false;

    private AugaryManager mAugaryManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.device_id = "demo_" + Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID;


        // Create the configuration

        AugaryConfig sconfig = new AugaryConfig();

        sconfig.setContext(getApplicationContext());
        sconfig.setDeviceID(device_id);

        // This is our internal testing platform. Feel free to leave it as it is. For production, yopu should
        // replace it with your company name or an agreed upon platform name.
        // ie. sconfig.setPlatformID("ACME");
        sconfig.setPlatformID("testplatform");

        sconfig.setUsingFollowTracking(AugaryFollowTracking.DEVICE_AND_UPLOAD);
        sconfig.setNetworkPolicy(AugaryNetworkPolicy.IMMEDIATE);
        sconfig.setVideoSeconds(8);
        sconfig.setUsingBlackbox(AugaryVideoTracking.ON);
        sconfig.setUsingFollowTracking(AugaryFollowTracking.DEVICE_AND_UPLOAD);
        sconfig.setUsingSpeedingTracking(AugarySpeedingTracking.OFF);
        sconfig.setAugaryBlackboxTriggerSensitivity(AugaryTriggerThreshold.VERY_LOW);
        sconfig.setAugaryVideoQuality(AugaryVideoQuality.MEDIUM);
        sconfig.setOrientation(1);


        // Initialize Augary Manager
        try {
            mAugaryManager = new AugaryManager(sconfig);
        } catch (Exception e) {
            Log.e("Augary", "Exception when initializing AugaryManager: " + e.getMessage());
            System.exit(1);
        }





        // UI stuff
        initScreen();
        mHandler = new Handler();
        startRepeatingTask();


    }

    @Override public void onResume()
    {
        super.onResume();

        // Start Augary Manager. This has no effect if it is already started.
        try {
            mAugaryManager.start();
        } catch (Exception e) {
            Log.e("Augary", "Exception when starting AugaryManager: " + e.getMessage());
            // Note for devs: Since no stop() has been called, no exception will be thrown.
        }
    }


    /*
        UI related stuff.
     */

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_poweroff)
        {
            try {
                mAugaryManager.stop();
                mAugaryManager.finalize();
            }
            catch (Exception e)
            {
                Log.e("###", "Exception on exit:" + e);
            }

            System.exit(0);
        }
        else if (id == R.id.btn_setup)
        {
            this.previewing = true;
            initScreen();
            ImageView iv  = (ImageView) findViewById(R.id.imagePreview);
            mAugaryManager.turnPreviewOn(this, iv);
        }
        else if (id == R.id.btn_trigger)
        {
            if (!mAugaryManager.isRecording()) {
                try {
                    mAugaryManager.triggerRecording("Manual");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else    // Touched the screen in setup mode.
        {
            mAugaryManager.turnPreviewOff();
            this.previewing = false;
            initScreen();

            try {
                mAugaryManager.start();
            } catch (Exception e) {
                Log.e("Augary", "Exception when starting AugaryManager: " + e.getMessage());
                // Note for devs: Since no stop() has been called, no exception will be thrown here.
            }
        }
    }


    private void initScreen() {
        if (previewing)
        {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                setContentView(R.layout.preview);

            this.btnPreview = (Button) findViewById(R.id.button2);
            this.btnPreview.setOnClickListener(this);


        }
        else {

            setContentView(R.layout.main);
            startRepeatingTask();

            TextView tv = (TextView) findViewById(R.id.textView);
            tv.setText("Device id: " + device_id);


            this.btnSetup = (Button) findViewById(R.id.btn_setup);
            this.btnSetup.setOnClickListener(this);


            this.btnReboot = (Button) findViewById(R.id.btn_poweroff);
            this.btnReboot.setOnClickListener(this);


            this.btnTrigger = (Button) findViewById(R.id.btn_trigger);
            this.btnTrigger.setOnClickListener(this);
        }
    }




    // More UI stuff

    private int mInterval = 500; // 0.5 seconds by default, can be changed later
    private Handler mHandler;



    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {

            try {
                if (mAugaryManager != null && mAugaryManager.isRecording()) {
                    TextView tv = (TextView) findViewById(R.id.textView5);
                    tv.setText("Event triggered");
                    tv.setTextColor(Color.RED);
                } else {
                    TextView tv = (TextView) findViewById(R.id.textView5);
                    tv.setText("Awaiting event");
                    tv.setTextColor(Color.GREEN);
                }

                mHandler.postDelayed(mStatusChecker, mInterval);
            }
            catch ( Exception e) {};
        }
    };


    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
