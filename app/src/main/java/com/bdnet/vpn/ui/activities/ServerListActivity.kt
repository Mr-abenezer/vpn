package com.bdnet.vpn.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bdnet.vpn.R
import com.bdnet.vpn.data.local.AppDatabase
import com.bdnet.vpn.data.model.Server
import com.bdnet.vpn.ui.adapters.ServerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServerListActivity : AppCompatActivity(), ServerAdapter.OnServerClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var searchView: SearchView
    private lateinit var adapter: ServerAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_list)

        database = AppDatabase.getInstance(this)

        initViews()
        setupRecyclerView()
        loadServers()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        searchView = findViewById(R.id.search_view)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.servers)

        findViewById<View>(R.id.toolbar).findViewById<View>(R.id.toolbar_title)?.let {
            (it as? TextView)?.text = getString(R.string.servers)
        }

        swipeRefresh.setOnRefreshListener {
            loadServers()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = ServerAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadServers() {
        lifecycleScope.launch {
            database.serverDao().getAllServers().collectLatest { servers ->
                adapter.submitList(servers.map { Server.fromEntity(it) })
                swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onServerClick(server: Server) {
        // Save selection and go back
        lifecycleScope.launch {
            database.serverDao().updateLastUsed(server.id)
            Toast.makeText(this@ServerListActivity, "Selected: ${server.name}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
