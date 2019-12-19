package com.ecosa.devicemovementtracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.agrawalsuneet.dotsloader.loaders.TrailingCircularDotsLoader;
import com.ecosa.devicemovementtracker.R;
import com.ecosa.devicemovementtracker.Util.RuntimePermissionHelper;

import butterknife.ButterKnife;

public class Splash extends AppCompatActivity implements RuntimePermissionHelper.permissionInterface {
    public static Activity activity;

    RuntimePermissionHelper runtimePermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        runtimePermissionHelper = new RuntimePermissionHelper(Splash.this, this);
        activity = this;
        runtimePermissionHelper.requestLocationPermission(1);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3 * 1000);
                    loader_animation();

                    startActivity(new Intent(Splash.this, MainActivity.class));

                    finish();


                } catch (Exception ex) {

                }
            }
        };
        thread.start();
    }

    public void loader_animation() {
        TrailingCircularDotsLoader trailingCircularDotsLoader = new TrailingCircularDotsLoader(
                this,
                8,
                ContextCompat.getColor(this, android.R.color.holo_green_light),
                100,
                5);
        trailingCircularDotsLoader.setAnimDuration(900);
        trailingCircularDotsLoader.setAnimDelay(100);


    }


    @Override
    public void onSuccessPermission(int code) {

    }

}
