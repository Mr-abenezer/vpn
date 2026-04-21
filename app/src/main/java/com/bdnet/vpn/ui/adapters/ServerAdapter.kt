package com.bdnet.vpn.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bdnet.vpn.R
import com.bdnet.vpn.data.model.Server

class ServerAdapter(
    private val onServerClick: OnServerClickListener
) : ListAdapter<Server, ServerAdapter.ServerViewHolder>(ServerDiffCallback()) {

    interface OnServerClickListener {
        fun onServerClick(server: Server)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ServerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flagImage: ImageView = itemView.findViewById(R.id.server_flag)
        private val serverName: TextView = itemView.findViewById(R.id.server_name)
        private val serverCarrier: TextView = itemView.findViewById(R.id.server_carrier)
        private val protocolIndicator: TextView = itemView.findViewById(R.id.protocol_indicator)
        private val signalIndicator: ImageView = itemView.findViewById(R.id.signal_indicator)

        fun bind(server: Server) {
            serverName.text = server.name
            serverCarrier.text = server.carrier ?: server.countryCode
            protocolIndicator.text = server.protocol.uppercase()

            // Load flag
            val flagResId = itemView.context.resources.getIdentifier(
                "flag_${server.countryCode.lowercase()}",
                "drawable",
                itemView.context.packageName
            )
            if (flagResId != 0) {
                flagImage.setImageResource(flagResId)
            } else {
                flagImage.setImageResource(R.drawable.ic_flag_default)
            }

            // Signal strength based on ping
            signalIndicator.setImageResource(
                when {
                    server.ping == null -> R.drawable.ic_signal_unknown
                    server.ping < 100 -> R.drawable.ic_signal_high
                    server.ping < 200 -> R.drawable.ic_signal_medium
                    else -> R.drawable.ic_signal_low
                }
            )

            itemView.setOnClickListener {
                onServerClick.onServerClick(server)
            }
        }
    }

    class ServerDiffCallback : DiffUtil.ItemCallback<Server>() {
        override fun areItemsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem == newItem
        }
    }
}
