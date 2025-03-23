package com.example.workshop2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize ImageView
        ImageView logo = findViewById(R.id.logo);

        // Load rotate and zoom animation
        Animation rotateZoomFadeOut = AnimationUtils.loadAnimation(this, R.anim.rotate_zoom_fade_out);
        logo.startAnimation(rotateZoomFadeOut);

        // Delay for 3 seconds before moving to the main activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SplashActivity
        }, 1700); // 3000 milliseconds = 3 seconds
    }
}
