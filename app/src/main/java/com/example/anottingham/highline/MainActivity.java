package com.example.anottingham.highline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see com.example.anottingham.highline.util.SystemUiHider
 */
public class MainActivity extends Activity implements View.OnClickListener, SensorEventListener{

    private MediaPlayer NorthPlayer, EastPlayer, SouthPlayer, WestPlayer, VOPlayer;
    private static SensorManager sensorService;
    private Sensor sensor;

    private boolean dbon = false;
    private LocationListener locationListener;
    private LocationManager locationManager = null;
    private AlertDialog dialog;

    Bundle extras = getIntent().getExtras();

    //coordinates of your polygon
    private static final LatLng [] REGION = {
            /*new LatLng(33.992527,-118.45536),
            new LatLng(33.993714,-118.453069),
            new LatLng(33.993536,-118.452704),
            new LatLng(33.991806,-118.453927)*/

            new LatLng(40.740933, -74.008087),
            new LatLng(40.740819, -74.007923),
            new LatLng(40.739638, -74.008154),
            new LatLng(40.739676, -74.008345),


            /*new LatLng(1,1),
            new LatLng(1,2),
            new LatLng(2,2),
            new LatLng(2,1)*/
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbon = extras.getBoolean("debug");


        LinearLayout linLayout = (LinearLayout) findViewById(R.id.mainnav);
        Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);


        linLayout.startAnimation(anim);


        //initialize view components
        Button exit_button = (Button) findViewById(R.id.exit_button);
        Button map_button = (Button) findViewById(R.id.map_button);
        Button history_button = (Button) findViewById(R.id.history_button);

        map_button.setOnClickListener(this);
        history_button.setOnClickListener(this);
        exit_button.setOnClickListener(this);

		/* Initialize MediaPlayers */
        if(dbon == false) {
            NorthPlayer = MediaPlayer.create(this, R.raw.north_train);
            EastPlayer = MediaPlayer.create(this, R.raw.east_train);
            SouthPlayer = MediaPlayer.create(this, R.raw.south_train);
            WestPlayer = MediaPlayer.create(this, R.raw.west_train);
            VOPlayer = MediaPlayer.create(this, R.raw.voiceover);
        }
        else{
            NorthPlayer = MediaPlayer.create(this, R.raw.north);
            EastPlayer = MediaPlayer.create(this, R.raw.east);
            SouthPlayer = MediaPlayer.create(this, R.raw.south);
            WestPlayer = MediaPlayer.create(this, R.raw.west);
        }

        VOPlayer.setLooping(false);
        //Error dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Error")
                .setMessage("Please return to the service area")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();

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
        checkGpsState();
        checkLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //to stop the listener and save battery
        if (sensor != null) {
            sensorService.unregisterListener(this);
        }
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaResources();
    }

    private void changeVolumeLevel(float azimuth) {

        float angle;
        if(azimuth <= 360 && azimuth >= 270) {
            angle = (azimuth - 270)/90;
            NorthPlayer.setVolume(angle, angle);
            angle = 1 - angle;
            WestPlayer.setVolume(angle, angle);
        }
        if(azimuth <= 270 && azimuth >= 180) {
            angle = (azimuth - 180)/90;
            WestPlayer.setVolume(angle, angle);
            angle = 1 - angle;
            SouthPlayer.setVolume(angle, angle);
        }
        if(azimuth <= 180 && azimuth >= 90) {
            angle = (azimuth - 90)/90;
            SouthPlayer.setVolume(angle, angle);
            angle = 1 - angle;
            EastPlayer.setVolume(angle, angle);
        }
        if(azimuth <= 90 && azimuth >= 0) {
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
        releasePlayer(VOPlayer);
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
        startPlayer(VOPlayer);
        VOPlayer.setLooping(false);
        VOPlayer.setVolume(0.4F,0.4F);
    }

    private void stopMediaPlayers() {
        stopPlayer(NorthPlayer);
        stopPlayer(EastPlayer);
        stopPlayer(SouthPlayer);
        stopPlayer(WestPlayer);
        stopPlayer(VOPlayer);
    }

    private void startPlayer(MediaPlayer mp) {
        if(!mp.isPlaying()) {
            mp.start();
            mp.setVolume(0, 0);
            //repeat song when it's stop
            mp.setLooping(true);
        }
    }

    private void stopPlayer(MediaPlayer mp) {
        if(mp.isPlaying()) {
            mp.stop();
        }
    }


    private void checkGpsState() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false)
                    .setTitle("The gps is disabled.")
                    .setMessage("You should activate it before start using application")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
            builder.show();
        }
    }

    private void checkLocationUpdates() {
        //Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(
                Context.LOCATION_SERVICE);

        //Define a listener that responds to location updates
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Called when a new location is found

                if( dbon == false && !pointIsInRegion(new LatLng(
                                location.getLatitude(),
                                location.getLongitude()), REGION)) {

                    if(!dialog.isShowing()) {
                        dialog.show();
                        stopMediaPlayers();
                    }

                } else {
                    if(dialog.isShowing()) {
                        dialog.dismiss();
                        startMediaPlayers();
                    }
                }

            }

            //not in use method's
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    public static boolean pointIsInRegion(LatLng point, LatLng[] region)
    {
        int crossings = 0;
        int count = region.length;
        // for each edge
        for (int i=0; i < count; i++)
        {
            LatLng a = region [i];
            int j = i + 1;
            if (j >= count)
            {
                j = 0;
            }
            LatLng b = region [j];
            if (rayCrossesSegment(point, a, b))
            {
                crossings++;
            }
        }
        // odd number of crossings?
        return (crossings % 2 == 1);
    }

    private static boolean rayCrossesSegment(LatLng point, LatLng a, LatLng b)
    {
        double px = point.longitude;
        double py = point.latitude;
        double ax = a.longitude;
        double ay = a.latitude;
        double bx = b.longitude;
        double by = b.latitude;
        if (ay > by)
        {
            ax = b.longitude;
            ay = b.latitude;
            bx = a.longitude;
            by = a.latitude;
        }
        // alter longitude to cater to 180 degree crossings
        if (px < 0) { px += 360; }
        if (ax < 0) { ax += 360; }
        if (bx < 0) { bx += 360; }

        if (py == ay || py == by) py += 0.00000001;
        if ((py > by || py < ay) || (px > Math.max(ax, bx))) return false;
        if (px < Math.min(ax, bx)) return true;

        double red = (ax != bx) ? ((by - ay) / (bx - ax)) : Double.MAX_VALUE;
        double blue = (ax != px) ? ((py - ay) / (px - ax)) : Double.MAX_VALUE;
        return (blue >= red);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.history_button:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                break;

            case R.id.map_button:
                startActivity(new Intent(MainActivity.this, MapActivity.class));
                break;

            case R.id.share_button:
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
        changeVolumeLevel(azimuth);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not in use
    }
}
