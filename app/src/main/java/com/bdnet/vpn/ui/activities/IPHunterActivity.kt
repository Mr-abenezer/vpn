package com.bdnet.vpn.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bdnet.vpn.R
import com.google.android.material.button.MaterialButton

class IPHunterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ip_hunter)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.ip_hunter)

        findViewById<MaterialButton>(R.id.hunt_button).setOnClickListener {
            fetchIpInfo()
        }
    }

    private fun fetchIpInfo() {
        // Implement IP lookup API call
        findViewById<TextView>(R.id.ip_result).text = "Fetching..."
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
