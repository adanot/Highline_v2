package com.example.anottingham.highline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;


public class SplashScreenActivity extends Activity implements View.OnClickListener {

    private FrameLayout frameLayout;
    private Animation anim;
    private Button production;
    private Button test;
    private Intent mainIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
        frameLayout = (FrameLayout) findViewById(R.id.splash_screen);
        anim = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.fade_out);
        production = (Button) findViewById(R.id.prodBtn);
        test = (Button) findViewById(R.id.testBtn);

        test.setOnClickListener(this);
        production.setOnClickListener(this);

        anim.setAnimationListener(new Animation.AnimationListener() {
            //start activity after fade out
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(mainIntent);
            }

            //not in use method's
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationStart(Animation animation) {}

        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.testBtn:
                mainIntent.putExtra("debug", true);
                frameLayout.startAnimation(anim);

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        //Create an intent that will start the main activity.
//                        Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
//                        SplashScreenActivity.this.startActivity(mainIntent);
//
//                        //Finish splash activity so user cant go back to it.
//                        SplashScreenActivity.this.finish();
//
//                        //Apply splash exit (fade out) and main entry (fade in) animation transitions.
//                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                    }
//                }, 2000);
                break;

            case R.id.prodBtn:
                mainIntent.putExtra("debug", false);
                frameLayout.startAnimation(anim);
                break;
        }

    }

}
