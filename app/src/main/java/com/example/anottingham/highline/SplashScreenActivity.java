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
    private Intent mainIntent;
    private FtpConnectTask connectTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
        frameLayout = (FrameLayout) findViewById(R.id.splash_screen);
        anim = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.fade_out);

        Button production = (Button) findViewById(R.id.prodBtn);
        Button test = (Button) findViewById(R.id.testBtn);
        Button checkUpdates = (Button) findViewById(R.id.updateBtn);

        test.setOnClickListener(this);
        production.setOnClickListener(this);
        checkUpdates.setOnClickListener(this);

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
                break;

            case R.id.prodBtn:
                mainIntent.putExtra("debug", false);
                frameLayout.startAnimation(anim);
                break;

            case R.id.updateBtn:
                connectTask = new FtpConnectTask(this);
                connectTask.execute();
                break;
        }

    }
}
