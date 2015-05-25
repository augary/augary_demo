package augary.demo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.augary.app.*;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    AugaryManager AM;

    private Button btnStop, btnStart, btnTrigger, btnKill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnStop = (Button) findViewById(R.id.btn_stop);
        this.btnStop.setOnClickListener(this);

        this.btnTrigger = (Button) findViewById(R.id.btn_trigger);
        this.btnTrigger.setOnClickListener(this);

        this.btnStart = (Button) findViewById(R.id.btn_start);
        this.btnStart.setOnClickListener(this);

        // Note, don't use the kill button. It's for testing purposses
        this.btnKill = (Button) findViewById(R.id.btn_kill);
        this.btnKill.setOnClickListener(this);

        AM = new AugaryManager(getApplicationContext(), "Augary", "TestDevice", false, false, false);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        AM.start();
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

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_trigger) {
            try {
                Log.i("###", "### TRIGGERING ###");

                AM.triggerRecording(null);
                //AM.triggerRecording();
            } catch (Exception e) {
                Log.i("###", "EXCEPTION ### in trigger recording ### " + e);
            }
        } else if (id == R.id.btn_stop) {
            try {
                Log.i("###", "### FINALIZE ###");
                AM.stop();
                //AM.Finalize();
            } catch (Exception e) {
                Log.e("###", "### Exception in TheNewDemo finalize " + e);
            }

        } else if (id == R.id.btn_start) {
            Log.i("###", "### RESTART ###");
            //AM = new AugaryManager(getBaseContext(), "augary-kiosk-3", true, true, false);
            //AM.Start();
            AM.start();
        } else if (id == R.id.btn_kill) {
            Log.i("###", "### KILLING IN THE NAME OF ###");
            //AM = new AugaryManager(getBaseContext(), "augary-kiosk-3", true, true, false);
            //AM.Start();
            try {
                Log.i("###", "### FINALIZE ###");
                AM.stop();
                //AM.Finalize();
            } catch (Exception e) {
                Log.e("###", "### Exception in TheNewDemo finalize " + e);
            }
            AM = null;
        }
    }
}
