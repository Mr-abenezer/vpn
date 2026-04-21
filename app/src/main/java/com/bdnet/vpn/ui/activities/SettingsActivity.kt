package com.bdnet.vpn.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.bdnet.vpn.R
import com.bdnet.vpn.util.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {

    private val dataStore by preferencesDataStore(name = "vpn_prefs")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        loadSettings()
        setupClickListeners()
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val prefs = dataStore.data.first()
            val autoConnect = prefs[androidx.datastore.preferences.core.booleanPreferencesKey(Constants.PREF_AUTO_CONNECT)] ?: false
            val killSwitch = prefs[androidx.datastore.preferences.core.booleanPreferencesKey(Constants.PREF_KILL_SWITCH)] ?: false
            val splitTunnel = prefs[androidx.datastore.preferences.core.booleanPreferencesKey(Constants.PREF_SPLIT_TUNNEL_ENABLED)] ?: false

            findViewById<SwitchCompat>(R.id.switch_auto_connect)?.isChecked = autoConnect
            findViewById<SwitchCompat>(R.id.switch_kill_switch)?.isChecked = killSwitch
            findViewById<SwitchCompat>(R.id.switch_split_tunnel)?.isChecked = splitTunnel
        }
    }

    private fun setupClickListeners() {
        findViewById<SwitchCompat>(R.id.switch_auto_connect).setOnCheckedChangeListener { _, isChecked ->
            saveSetting(Constants.PREF_AUTO_CONNECT, isChecked)
        }

        findViewById<SwitchCompat>(R.id.switch_kill_switch).setOnCheckedChangeListener { _, isChecked ->
            saveSetting(Constants.PREF_KILL_SWITCH, isChecked)
        }

        findViewById<SwitchCompat>(R.id.switch_split_tunnel).setOnCheckedChangeListener { _, isChecked ->
            saveSetting(Constants.PREF_SPLIT_TUNNEL_ENABLED, isChecked)
        }

        findViewById<android.view.View>(R.id.dns_settings_item).setOnClickListener {
            // Open DNS settings dialog
        }
    }

    private fun saveSetting(key: String, value: Boolean) {
        lifecycleScope.launch {
            dataStore.edit { prefs ->
                prefs[androidx.datastore.preferences.core.booleanPreferencesKey(key)] = value
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
