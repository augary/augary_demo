package augary.demo;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import android.provider.Settings.Secure;



import com.augary.app.*;

public class MainActivity extends Activity implements View.OnClickListener  , SurfaceHolder.Callback {




    /***********************************************************************
     *
     *
     *            Camera Stuff
     *
     *
     ***********************************************************************/


    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;



    public void initCamera() {

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        refreshCamera();
    }


    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // open the camera
            camera = Camera.open();

        } catch (RuntimeException e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
        Camera.Parameters param;
        //param = camera.getParameters();

        // modify parameter
        //param.setPreviewSize(352, 288);
        //camera.setParameters(param);
        try {
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }


    /**********************************************
     *
     *
     *       Other stuff
     *
     *
     *********************************************************88*/



    AugaryManager AM = null;
    private boolean started = false;

    private Button btnStop, btnStart, btnTrigger, btnKill, btnInitialize;
    private Button btnPreview;
    private Button btnPreview2;

    private int state = 0 ;
    // state == 0   -->   config screen
    // state == 1   -->   alignment screen
    // state == 2   -->   main screen

    //private CameraPreview mPreview;
    private RelativeLayout relativeLayout;
    private MainActivity myContext;
    private Camera mCamera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_screen);


            String android_id = Secure.getString(getContentResolver(),Secure.ANDROID_ID);

            EditText tv = (EditText) findViewById(R.id.editText2);
            tv.setText(""+android_id);

        this.btnInitialize = (Button) findViewById(R.id.btn_initialize);
        this.btnInitialize.setOnClickListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.myContext = this;


        // Normally we would put this here, but we're using the configuration startup screen instead
        //  AM = new AugaryManager(getApplicationContext(), "Augary-demo", "TestDevice", true, false, false);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "Landscape mode implemented", Toast.LENGTH_LONG).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "Please implement portrait mode !!!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    boolean previewing = false;

    @Override
    public void onResume() {
        super.onResume();
        if (AM != null && started == true) {
            AM.start();
            notice("Started");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void notice (String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    // Enable the stop button on timeout
    public void checkstop(final int seconds)
    {

        Runnable checkstoprunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(seconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("###", "checkstop");
                while (AM.isRecording()) try {
                    Log.e("###", "checkstop is recording, waiting");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (started) {
                    Log.e("###", "checkstop enabling");runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnStop.setEnabled(true);
                        }
                    });

                }
            }
        };
        Thread checkStopThread = new Thread(checkstoprunnable);
        checkStopThread.start();
    }

    // Enable/disable the trigger button
    public void checktrigger()
    {
         Runnable checkTriggerRunnable = new Runnable() {
            @Override
            public void run() {
                Log.e("###", "checktrigger");
                while (true) {
                    if (started && !AM.isRecording())
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTrigger.setEnabled(true);
                            }
                        });

                    else
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnTrigger.setEnabled(false);
                            }
                        });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread checkTriggerThread = new Thread(checkTriggerRunnable);
        checkTriggerThread.start();
    }
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_trigger) {
            try {
                Log.i("###", "### TRIGGERING ###");

                AM.triggerRecording(null);
                btnTrigger.setEnabled(false);
                btnStop.setEnabled(false);
                checkstop(15);
                //AM.triggerRecording();
                notice("Recording triggered");
            } catch (Exception e) {
                Log.i("###", "EXCEPTION ### in trigger recording ### " + e);
                notice("Recording already in progress");
            }

        } else if (id == R.id.btn_stop) {
            try {
                Log.i("###", "### FINALIZE ###");
                AM.stop();
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnTrigger.setEnabled(false);
                started = false;
                //AM.Finalize();
                notice("Stopped");
            } catch (Exception e) {
                Log.e("###", "### Exception in TheNewDemo finalize " + e);
                notice("Please wait 10 seconds after starting before stopping.");
            }


        } else if (id == R.id.btn_start) {
            Log.i("###", "### RESTART ###");
            //AM = new AugaryManager(getBaseContext(), "augary-kiosk-3", true, true, false);
            //AM.Start();
            AM.start();

            started = true;
            btnStart.setEnabled(false);

            btnTrigger.setEnabled(false);

            notice("Started");
        }
        else if (id == R.id.btn_initialize)
        {
            EditText platform_id = (EditText)this.findViewById(R.id.editText);
            EditText device_id = (EditText)this.findViewById(R.id.editText2);

            CheckBox use_follow_distance = (CheckBox) this.findViewById(R.id.checkBox);
            CheckBox use_black_box = (CheckBox) this.findViewById(R.id.checkBox2);
            CheckBox use_augary_bbox_triggers = (CheckBox) this.findViewById(R.id.checkBox3);

            int orientation = -1;

            AM = new AugaryManager(
                    getApplicationContext(),                    // Application context, used for writing some files to disk
                    platform_id.getText().toString(),           // Platform ID set by you to uniquely identify your platform devices. Ex: EastIndiaTradingCompany
                    device_id.getText().toString(),             // Device ID set by you to uniquely identify your individual devices. Ex: FlyingDutchman0032
                    use_black_box.isChecked(),                  // Enable use of Black Box module
                    use_follow_distance.isChecked(),            // Enable use of Following Distance module
                    use_augary_bbox_triggers.isChecked(),       // Enable Augary's custom triggers for Black Box recording (triggerRecording() can always be used by developer regardless of this flag)
                    orientation,                                // orientation of the device
                    1,                                         // JPEG quality
                    3                                           // seconds to record before and after
            );

            setContentView(R.layout.alignment_screen);

            this.btnPreview = (Button) findViewById(R.id.btn_preview);
            this.btnPreview.setOnClickListener(this);


            this.btnPreview2 = (Button) findViewById(R.id.btn_preview2);
            this.btnPreview2.setOnClickListener(this);

            /*
            this.btnPreview3 = (Button) findViewById(R.id.btn_preview3);
            this.btnPreview3.setOnClickListener(this);

            this.btnPreview4 = (Button) findViewById(R.id.btn_preview4);
            this.btnPreview4.setOnClickListener(this);
            */


            initCamera();

        } else if (id == R.id.btn_layout)
        {
            setContentView(R.layout.main_screen);

            this.btnStop = (Button) findViewById(R.id.btn_stop);
            this.btnStop.setOnClickListener(this);

            this.btnTrigger = (Button) findViewById(R.id.btn_trigger);
            this.btnTrigger.setOnClickListener(this);

            this.btnStart = (Button) findViewById(R.id.btn_start);
            this.btnStart.setOnClickListener(this);


            btnStart.setEnabled(false);
            btnTrigger.setEnabled(false);
            btnStop.setEnabled(false);
            checkstop(10);
            checktrigger();


            //releaseCamera();

            AM.start();
            started = true;
            notice("Augary Started");

        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /*
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result=null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }



    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }*/

}