package com.example.anottingham.highline;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see com.example.anottingham.highline.util.SystemUiHider
 */
public class MainActivity extends Activity implements View.OnClickListener, SensorEventListener{

    private CompassView compassView;

    private MediaPlayer NorthPlayer, EastPlayer, SouthPlayer, WestPlayer, voiceOver;
    private static SensorManager sensorService;
    private Sensor sensor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize view components
        compassView = new CompassView(this);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        frameLayout.addView(compassView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
        );

        Button exit_button = (Button) findViewById(R.id.exit_button);
        exit_button.setOnClickListener(this);

        Button settings_button = (Button) findViewById(R.id.settings_button);
        settings_button.setOnClickListener(this);

		/* Initialize MediaPlayers */
        NorthPlayer = MediaPlayer.create(this, R.raw.north);
        EastPlayer = MediaPlayer.create(this, R.raw.east);
        SouthPlayer = MediaPlayer.create(this, R.raw.south);
        WestPlayer = MediaPlayer.create(this, R.raw.west);

        voiceOver = MediaPlayer.create(this, R.raw.voiceover);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            sensorService.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("Compass MainActivity", "Registered for ORIENTATION Sensor");
        } else {
            Log.e("Compass MainActivity", "Registered for ORIENTATION Sensor");
            Toast.makeText(this, "ORIENTATION Sensor not found",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        startMediaPlayers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //to stop the listener and save battery
        if (sensor != null) {
            sensorService.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaResources();
    }

    /* Method to control button interactions */

    //TODO: Create settings class
    /*
    public void startSettingsActivity(){

        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
    */

    private void changeVolumeLevel(float azimuth) {

        float angle;
        if(azimuth <= 360 && azimuth > 270) {
            angle = (azimuth - 270)/90;
            NorthPlayer.setVolume(angle, angle);
            angle = 1 - angle;
            WestPlayer.setVolume(angle, angle);
        }
        if(azimuth <= 270 && azimuth > 180) {
            angle = (azimuth - 180)/90;
            WestPlayer.setVolume(angle, angle);
            angle = 1 - angle;
            SouthPlayer.setVolume(angle, angle);
        }
        if(azimuth <= 180 && azimuth > 90) {
            angle = (azimuth - 90)/90;
            SouthPlayer.setVolume(angle, angle);
            angle = 1 - angle;
            EastPlayer.setVolume(angle, angle);
        }
        if(azimuth <= 90 && azimuth > 0) {
            angle = azimuth/90;
            EastPlayer.setVolume(angle, angle);
            angle = 1 - angle;
            NorthPlayer.setVolume(angle, angle);
        }
    }

    private void releaseMediaResources() {
       releasePlayer(NorthPlayer);
       releasePlayer(EastPlayer);
       releasePlayer(SouthPlayer);
       releasePlayer(WestPlayer);
    }

    private void releasePlayer(MediaPlayer mp) {
        if(mp != null) {
            mp.release();
            mp = null;
        }
    }

    private void startMediaPlayers() {
        startPlayer(NorthPlayer);
        startPlayer(EastPlayer);
        startPlayer(SouthPlayer);
        startPlayer(WestPlayer);
    }

    private void startPlayer(MediaPlayer mp) {
        mp.start();
        mp.setVolume(0, 0);
        //repeat song when it's stop
        mp.setLooping(true);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.settings_button:
                //startSettingsActivity();
                break;

            case R.id.exit_button:
                android.os.Process.killProcess(android.os.Process.myPid());
                break;

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // angle between the magnetic north direction
        // 0=North, 90=East, 180=South, 270=West
        float azimuth = event.values[0];
        compassView.updateData(azimuth);
        Log.i("Angle Direction", String.valueOf(azimuth));
        changeVolumeLevel(azimuth);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in use
    }
}
