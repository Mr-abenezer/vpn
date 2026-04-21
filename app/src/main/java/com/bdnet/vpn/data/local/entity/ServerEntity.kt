package com.bdnet.vpn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "host")
    val host: String,

    @ColumnInfo(name = "port")
    val port: Int,

    @ColumnInfo(name = "protocol")
    val protocol: String,

    @ColumnInfo(name = "payload")
    val payload: String? = null,

    @ColumnInfo(name = "uuid")
    val uuid: String? = null,

    @ColumnInfo(name = "path")
    val path: String? = null,

    @ColumnInfo(name = "sni")
    val sni: String? = null,

    @ColumnInfo(name = "network")
    val network: String? = null,

    @ColumnInfo(name = "security")
    val security: String? = null,

    @ColumnInfo(name = "encryption")
    val encryption: String? = null,

    @ColumnInfo(name = "country_code")
    val countryCode: String,

    @ColumnInfo(name = "carrier")
    val carrier: String? = null,

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "is_premium")
    val isPremium: Boolean = false,

    @ColumnInfo(name = "ping")
    val ping: Long? = null,

    @ColumnInfo(name = "last_used")
    val lastUsed: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
