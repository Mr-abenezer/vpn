package com.bdnet.vpn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tweaks")
data class TweakEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "payload")
    val payload: String,

    @ColumnInfo(name = "host")
    val host: String? = null,

    @ColumnInfo(name = "port")
    val port: Int? = null,

    @ColumnInfo(name = "method")
    val method: String? = null,

    @ColumnInfo(name = "headers")
    val headers: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
