package com.example.anottingham.highline;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anottingham.highline.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity {

    //private static final String LOGTAG = "HighLine";
    Button exit_button, settings_button;
    MediaPlayer NorthPlayer, EastPlayer, SouthPlayer, WestPlayer;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    Float ALPHA = 0.03f;
    Float azimuth = null;
    Float pi = (float)Math.PI;

    TextView northText = (TextView) findViewById(R.id.northView);
    TextView eastText = (TextView) findViewById(R.id.eastview);
    TextView southText = (TextView) findViewById(R.id.southView);
    TextView westText = (TextView) findViewById(R.id.westView);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Sensor(compass) setup
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mAccelerometer != null) {
            mSensorManager.registerListener(mySensorEventListener, mAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("Compass MainActivity", "Registered for ORIENTATION Sensor");
        } else {
            Log.e("Compass MainActivity", "Registered for ORIENTATION Sensor");
            Toast.makeText(this, "ORIENTATION Sensor not found",
                    Toast.LENGTH_LONG).show();
            finish();
        }


        exit_button = (Button) findViewById(R.id.exit_button);
        exit_button.setOnClickListener((android.view.View.OnClickListener) this);

        settings_button = (Button) findViewById(R.id.settings_button);
        settings_button.setOnClickListener((android.view.View.OnClickListener) this);

		/* Initialize MediaPlayers */
        NorthPlayer = MediaPlayer.create(this, R.raw.north);
        EastPlayer = MediaPlayer.create(this, R.raw.east);
        SouthPlayer = MediaPlayer.create(this, R.raw.south);
        WestPlayer = MediaPlayer.create(this, R.raw.west);

        NorthPlayer.start();
        EastPlayer.start();
        SouthPlayer.start();
        WestPlayer.start();
    }



    /* Method to control button interactions */
    public void onClick(View view){
        switch (view.getId()){
            case R.id.settings_button:
                //startSettingsActivity();
                break;

            case R.id.exit_button:
                android.os.Process.killProcess(android.os.Process.myPid());
                break;

        }
    }
    //TODO: Create settings class
    /*
    public void startSettingsActivity(){

        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
    */

    //Custom low pass filter to smooth input to output
    protected float lowPass( Float input, Float output ) {
        if ( output == null ) return input;
        output = output + ALPHA * (input - output);
        return output;
    }

    //Scale the range 0-2pi to 0-1 for setting the player volumes
    //This equation is simplified down
    public Float scale(Float azimuth, Float min, Float max){

        return (((azimuth - min))/(max-min));
    }

    //Using the scaled volume, set the appropriate player's volumes
    public void setNorthEast(Float scaledVol){
        NorthPlayer.setVolume(1-scaledVol,1-scaledVol);
        EastPlayer.setVolume(scaledVol, scaledVol);
        SouthPlayer.setVolume(0,0);
        WestPlayer.setVolume(0, 0);

        northText.setText("North: "+Float.toString(1-scaledVol));
        eastText.setText("East: "+Float.toString(scaledVol));
        southText.setText("South: 0");
        westText.setText("West: 0");


    }

    public void setSouthEast(Float scaledVol){
        NorthPlayer.setVolume(0,0);
        EastPlayer.setVolume(1-scaledVol,1-scaledVol);
        SouthPlayer.setVolume(scaledVol, scaledVol);
        WestPlayer.setVolume(0,0);

        northText.setText("North: 0");
        eastText.setText("East: "+Float.toString(1-scaledVol));
        southText.setText("South: "+Float.toString(scaledVol));
        westText.setText("West: 0");
    }

    public void setSouthWest(Float scaledVol){
        NorthPlayer.setVolume(0,0);
        EastPlayer.setVolume(0,0);
        SouthPlayer.setVolume(1-scaledVol,1-scaledVol);
        WestPlayer.setVolume(scaledVol, scaledVol);

        northText.setText("North: 0");
        eastText.setText("East: 0");
        southText.setText("South: "+Float.toString(1-scaledVol));
        westText.setText("West: "+Float.toString(scaledVol));
    }

    public void setNorthWest(Float scaledVol){
        NorthPlayer.setVolume(scaledVol, scaledVol);
        EastPlayer.setVolume(0,0);
        SouthPlayer.setVolume(0,0);
        WestPlayer.setVolume(1-scaledVol,1-scaledVol);

        northText.setText("North: "+Float.toString(scaledVol));
        eastText.setText("East: 0");
        southText.setText("South: 0");
        westText.setText("West: "+Float.toString(1-scaledVol));
    }



    // Event listener for compass sensor.
    //On change method implements smoothing with lopass filter and converts from radian to deg
    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            azimuth = lowPass(event.values[0], azimuth);

            if(azimuth >= 0 || azimuth < (pi/2)){

                setNorthEast(scale(azimuth, 0f, (pi/2)));
            }

            if(azimuth >= (pi/2) || azimuth < pi){

                setSouthEast(scale(azimuth, (pi/2), pi));
            }

            if(azimuth >= pi || azimuth < ((3*pi)/2)){

                setSouthWest((scale(azimuth, pi, (3*pi)/2)));
            }

            if(azimuth >= ((3*pi)/2) || azimuth < (2*pi)){

                setNorthWest((scale(azimuth, (3*pi/2), (2*pi))));
            }


        }


    };
}
