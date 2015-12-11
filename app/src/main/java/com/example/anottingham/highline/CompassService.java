package com.example.anottingham.highline;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class CompassService extends Service {
    private MediaPlayer NorthPlayer, EastPlayer, SouthPlayer, WestPlayer;
    private static SensorManager sensorService;
    private Sensor sensor;
    private SensorEventListener listener;
    private BroadcastReceiver mReceiver;
    private final Float northOffset = 45F;

    private float attenuation;

    private MyBinder binder = new MyBinder();

    class MyBinder extends Binder {
        CompassService getService() {
            return CompassService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NorthPlayer = MediaPlayer.create(this, R.raw.train_north);
        EastPlayer = MediaPlayer.create(this, R.raw.train_east);
        SouthPlayer = MediaPlayer.create(this, R.raw.train_south);
        WestPlayer = MediaPlayer.create(this, R.raw.train_west);

        listener = new compassSensor();
        sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            sensorService.registerListener(listener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("Compass MainActivity", "Registered for ORIENTATION Sensor");
        } else {
            Log.e("Compass MainActivity", "Registered for ORIENTATION Sensor");
            Toast.makeText(this, "ORIENTATION Sensor not found",
                    Toast.LENGTH_LONG).show();
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);


    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d("CompassService", "Destroyed");
        sensorService.unregisterListener(listener);
        unregisterReceiver(mReceiver);
        releaseMediaResources();
        super.onDestroy();

    }

    private class compassSensor implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            float azimuth = event.values[0];
            changeVolumeLevel(azimuth, attenuation);
            Log.d("CompassService", String.valueOf(azimuth));
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //not in use
        }
    }


    private float smoothingSound(float angle) {
        if(angle > 90) {
            angle = 1;
        }
        return angle;
    }

    private void changeVolumeLevel(float azimuth, float attenuation) {

        float angle;
        if(azimuth < northOffset){
            azimuth = (360 - northOffset) + azimuth;
        }
        else{
            azimuth = azimuth - northOffset;
        }

        if(azimuth <= 360 && azimuth >= 270) {
            angle = (azimuth - 270)/90;
            angle = smoothingSound(angle);
            NorthPlayer.setVolume(angle - attenuation, angle - attenuation);
            angle = 1 - angle;
            WestPlayer.setVolume(angle - attenuation, angle - attenuation);
        }
        if(azimuth <= 270 && azimuth >= 180) {
            angle = (azimuth - 180)/90;
            angle = smoothingSound(angle);
            WestPlayer.setVolume(angle - attenuation, angle - attenuation);
            angle = 1 - angle;
            SouthPlayer.setVolume(angle - attenuation, angle - attenuation);
        }
        if(azimuth <= 180 && azimuth >= 90) {
            angle = (azimuth - 90)/90;
            angle = smoothingSound(angle);
            SouthPlayer.setVolume(angle - attenuation, angle - attenuation);
            angle = 1 - angle;
            EastPlayer.setVolume(angle - attenuation, angle - attenuation);
        }
        if(azimuth <= 90 && azimuth >= 0) {
            angle = azimuth/90;
            angle = smoothingSound(angle);
            EastPlayer.setVolume(angle - attenuation, angle - attenuation);
            angle = 1 - angle;
            NorthPlayer.setVolume(angle - attenuation, angle - attenuation);
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

    public void startMediaPlayers() {
        startPlayer(NorthPlayer);
        startPlayer(EastPlayer);
        startPlayer(SouthPlayer);
        startPlayer(WestPlayer);
    }

    public void pauseMediaPlayers() {
        pausePlayer(NorthPlayer);
        pausePlayer(EastPlayer);
        pausePlayer(SouthPlayer);
        pausePlayer(WestPlayer);
    }

    private void startPlayer(MediaPlayer mp) {
        if(!mp.isPlaying()) {
            mp.start();
            attenuation = 0.5f;
            mp.setVolume(0, 0);
            //repeat song
            mp.setLooping(true);

            // Create new thread for timing
            Runnable audioWait = new Runnable() {
                public void run() {

                    //Set sleep time to trigger full audio
                    SystemClock.sleep(10000);

                    attenuation = 0f;
                }
            };

            Thread waitThread = new Thread(audioWait);

            waitThread.start();
        }
    }

    private void pausePlayer(MediaPlayer mp) {
        mp.pause();
    }

    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Unregister and register listener after screen goes off.
                refreshListener ();
            }
        }
    }

    private void refreshListener(){
        sensorService.unregisterListener(listener);
        sensorService.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
