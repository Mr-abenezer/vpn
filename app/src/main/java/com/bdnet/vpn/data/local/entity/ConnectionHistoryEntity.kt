package com.bdnet.vpn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "connection_history",
    foreignKeys = [
        ForeignKey(
            entity = ServerEntity::class,
            parentColumns = ["id"],
            childColumns = ["server_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("server_id")]
)
data class ConnectionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "server_id")
    val serverId: Long? = null,

    @ColumnInfo(name = "server_name")
    val serverName: String? = null,

    @ColumnInfo(name = "start_time")
    val startTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "bytes_in")
    val bytesIn: Long = 0,

    @ColumnInfo(name = "bytes_out")
    val bytesOut: Long = 0,

    @ColumnInfo(name = "duration")
    val duration: Long = 0,

    @ColumnInfo(name = "protocol")
    val protocol: String? = null
)
