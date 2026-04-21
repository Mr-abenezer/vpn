package com.bdnet.vpn.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bdnet.vpn.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.about)

        // Set version
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            findViewById<TextView>(R.id.version_text).text = "Version ${packageInfo.versionName}"
        } catch (e: Exception) {
            findViewById<TextView>(R.id.version_text).text = "Version 1.0.3"
        }

        // Setup links
        findViewById<TextView>(R.id.telegram_link).setOnClickListener {
            openUrl("https://t.me/bdnetvpn")
        }

        findViewById<TextView>(R.id.facebook_link).setOnClickListener {
            openUrl("https://facebook.com/bdnetvpn")
        }

        findViewById<TextView>(R.id.youtube_link).setOnClickListener {
            openUrl("https://youtube.com/@bdnetvpn")
        }

        findViewById<TextView>(R.id.website_link).setOnClickListener {
            openUrl("https://sulitnetsoft.com")
        }

        findViewById<TextView>(R.id.support_email).setOnClickListener {
            sendEmail("support@sulitnetsoft.com")
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun sendEmail(email: String) {
        try {
            startActivity(Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            })
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
