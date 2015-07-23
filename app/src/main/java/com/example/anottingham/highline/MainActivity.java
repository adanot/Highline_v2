package com.example.anottingham.highline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see com.example.anottingham.highline.util.SystemUiHider
 */
public class MainActivity extends Activity implements View.OnClickListener{

    private MediaPlayer VOPlayer;
    private boolean isMediaPlayersWorking = true;
    private boolean needToShow = true;
    private Button stopMediaPlayerButton;
    private Button voiceOver;

    private boolean isDebugOn = false;
    private LocationListener locationListener = null;
    private LocationManager locationManager = null;
    private AlertDialog dialog;

    boolean bound = false;
    ServiceConnection sConn;
    CompassService myService;

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

        isDebugOn = getIntent().getBooleanExtra("debug", false);

        LinearLayout linLayout = (LinearLayout) findViewById(R.id.mainnav);
        Animation anim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                checkGpsState();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        linLayout.startAnimation(anim);


        //initialize view components
        Button exit_button = (Button) findViewById(R.id.exit_button);
        Button map_button = (Button) findViewById(R.id.map_button);
        Button history_button = (Button) findViewById(R.id.history_button);
        voiceOver = (Button) findViewById(R.id.vo_toggle);
        stopMediaPlayerButton = (Button) findViewById(R.id.stop_button);


        VOPlayer = MediaPlayer.create(this, R.raw.voiceover);
        VOPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                myService.startMediaPlayers();
                voiceOver.setVisibility(View.GONE);
            }
        });
        VOPlayer.start();
        VOPlayer.setLooping(false);
        VOPlayer.setVolume(0.4F, 0.4F);

        map_button.setOnClickListener(this);
        history_button.setOnClickListener(this);
        exit_button.setOnClickListener(this);
        voiceOver.setOnClickListener(this);
        stopMediaPlayerButton.setOnClickListener(this);

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

        sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                myService = ((CompassService.MyBinder) binder).getService();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };

        Intent i = new Intent(MainActivity.this, CompassService.class);
        startService(i);
        bindService(i, sConn, 0);
        checkLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, CompassService.class));

        if(locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }

        if(VOPlayer != null) {
            VOPlayer.stop();
        }

    }

    private void checkGpsState() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ){
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
                            stopService(new Intent(MainActivity.this, CompassService.class));
                            finish();
                            System.exit(0);
                        }
                    });
            builder.show();
        }
    }

    private void checkLocationUpdates() {
        //Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(
                Context.LOCATION_SERVICE);

        //Define a listener that responds to location updates
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Called when a new location is found

                if( !isDebugOn && !pointIsInRegion(new LatLng(
                        location.getLatitude(),
                        location.getLongitude()), REGION)) {

                    if(!dialog.isShowing() && needToShow) {
                        dialog.show();
                        myService.pauseMediaPlayers();
                        needToShow = false;

                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                needToShow = true;
                            }
                        }, 15000);
                    }

                } else {
                    if(dialog.isShowing()) {
                        dialog.dismiss();
                        myService.startMediaPlayers();
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
                stopService(new Intent(MainActivity.this, CompassService.class));
                finish();
                System.exit(0);
                break;
            case R.id.stop_button:
                if(isMediaPlayersWorking) {
                    stopMediaPlayerButton.setText("Start music");
                    myService.pauseMediaPlayers();
                    isMediaPlayersWorking = false;
                } else {
                    stopMediaPlayerButton.setText("Stop music");
                    myService.startMediaPlayers();
                    isMediaPlayersWorking = true;
                }
                break;

            case R.id.vo_toggle:
                VOPlayer.stop();
                myService.startMediaPlayers();
                voiceOver.setVisibility(View.GONE);
                break;
        }
    }
}
