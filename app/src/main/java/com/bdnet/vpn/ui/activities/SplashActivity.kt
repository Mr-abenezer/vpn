package com.bdnet.vpn.ui.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bdnet.vpn.R
import com.bdnet.vpn.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splash_logo)
        val appName = findViewById<TextView>(R.id.splash_app_name)

        // Fade in animation
        val fadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)
        fadeIn.duration = 800
        fadeIn.interpolator = AccelerateDecelerateInterpolator()
        fadeIn.start()

        val fadeInText = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f)
        fadeInText.duration = 800
        fadeInText.interpolator = AccelerateDecelerateInterpolator()
        fadeInText.startDelay = 400
        fadeInText.start()

        // Check for updates and navigate to main screen
        lifecycleScope.launch {
            delay(Constants.ANIMATION_SPLASH_DURATION)
            checkForUpdates()
            navigateToMain()
        }
    }

    private suspend fun checkForUpdates() {
        // Check Firebase for app version
        delay(500) // Simulate check
    }

    private fun navigateToMain() {
        val intent = Intent(this, VPNClient::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
