package com.example.anottingham.highline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;


public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.splash_screen);
        Animation anim = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.fade_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            //start activity after fade out
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            }

            //not in use method's
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationStart(Animation animation) {}

        });
        frameLayout.startAnimation(anim);
    }
}
