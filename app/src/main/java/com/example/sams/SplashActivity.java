package com.example.sams;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 3000L; // 3 seconds to clearly see the brand logo
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean hasProceeded = false;

    private final Runnable proceedRunnable = this::proceedToNextScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure window background is dark midnight before view inflation to eliminate any white flash
        getWindow().setBackgroundDrawableResource(R.drawable.bg_splash_gradient);

        // Allow navigation bars while maintaining a full edge-to-edge aesthetic
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
            insetsController.setAppearanceLightStatusBars(false); // Dark status bar icons for dark splash
            insetsController.setAppearanceLightNavigationBars(false);
        }

        setContentView(R.layout.activity_splash);

        // Run subtle scale + fade animation on the logo and branding
        animateLogoAndBranding();

        // Allow user to tap anywhere to proceed instantly without waiting
        View root = findViewById(R.id.splashRoot);
        if (root != null) {
            root.setOnClickListener(v -> proceedToNextScreen());
        }

        // Post delay to automatically proceed
        handler.postDelayed(proceedRunnable, SPLASH_DELAY_MS);
    }

    private void animateLogoAndBranding() {
        View logoCard = findViewById(R.id.cardLogoContainer);
        View title = findViewById(R.id.tvSplashTitle);
        View college = findViewById(R.id.tvSplashCollege);

        if (logoCard != null) {
            AnimationSet animSet = new AnimationSet(true);
            animSet.setInterpolator(new AccelerateDecelerateInterpolator());

            ScaleAnimation scale = new ScaleAnimation(
                    0.82f, 1.0f,
                    0.82f, 1.0f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            );
            scale.setDuration(900);

            AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setDuration(900);

            animSet.addAnimation(scale);
            animSet.addAnimation(alpha);
            logoCard.startAnimation(animSet);
        }

        if (title != null) {
            AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setDuration(1000);
            alpha.setStartOffset(300);
            title.startAnimation(alpha);
        }

        if (college != null) {
            AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setDuration(1000);
            alpha.setStartOffset(500);
            college.startAnimation(alpha);
        }
    }

    private synchronized void proceedToNextScreen() {
        if (hasProceeded) return;
        hasProceeded = true;
        handler.removeCallbacks(proceedRunnable);

        UserManager userManager = UserManager.getInstance(this);
        User currentUser = userManager.getCurrentUser();

        Intent nextIntent;
        if (currentUser != null) {
            // Already signed in -> Go straight to HomeActivity
            nextIntent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            // Need to sign in or register -> Go to MainActivity
            nextIntent = new Intent(SplashActivity.this, MainActivity.class);
        }

        startActivity(nextIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(proceedRunnable);
    }
}
