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
    float ALPHA = 0.03f;
    Float azimuth = null;
    Float azimuthRad = null;

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
        NorthPlayer = MediaPlayer.create(this, R.raw.North);
        EastPlayer = MediaPlayer.create(this, R.raw.East);
        SouthPlayer = MediaPlayer.create(this, R.raw.South);
        WestPlayer = MediaPlayer.create(this, R.raw.West);

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

    public float scale(Float azimuth){

        return (azimuth-0/360)*1;
    }
    public void setNorthEast(Float azimuth){
    }


    public void setSouthEast(Float azimuth){

    }

    public void setSouthWest(Float azimuth){

    }

    public void setNorthWest(Float azimuth){

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
            azimuthRad = lowPass(event.values[0], azimuthRad);
            azimuth = ((float)Math.toDegrees(azimuthRad)+360)%360;

            if(azimuth >= 0 || azimuth < 90){

                //TODO: North-East
                setNorthEast(azimuth);
            }

            if(azimuth >= 90 || azimuth < 180){

                //TODO: South-East
                setSouthEast(azimuth);
            }
            if(azimuth >= 180 || azimuth < 270){

                //TODO: South-West
                setSouthWest(azimuth);
            }
            if(azimuth >= 270 || azimuth < 360){

                //TODO: North-West
                setNorthWest(azimuth);
            }

        }


    };
}
